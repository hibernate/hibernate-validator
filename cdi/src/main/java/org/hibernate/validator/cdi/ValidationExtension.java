/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cdi;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedCallable;
import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.inject.spi.WithAnnotations;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.validation.BootstrapConfiguration;
import jakarta.validation.Configuration;
import jakarta.validation.Constraint;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.executable.ExecutableType;
import jakarta.validation.executable.ValidateOnExecution;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.cdi.internal.InheritedMethodsHelper;
import org.hibernate.validator.cdi.internal.ValidationProviderHelper;
import org.hibernate.validator.cdi.internal.ValidatorBean;
import org.hibernate.validator.cdi.internal.ValidatorFactoryBean;
import org.hibernate.validator.cdi.internal.interceptor.ValidationEnabledAnnotatedType;
import org.hibernate.validator.cdi.internal.interceptor.ValidationInterceptor;
import org.hibernate.validator.cdi.internal.util.GetterPropertySelectionStrategyHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * A CDI portable extension which integrates Bean Validation with CDI. It registers the following objects:
 * <ul>
 * <li>
 * Beans for {@link ValidatorFactory} and {@link Validator} representing default validator factory and validator as
 * configured via {@code META-INF/validation.xml}. These beans will have the {@code Default} qualifier and in addition
 * the {@code HibernateValidator} qualifier if Hibernate Validator is the default validation provider.</li>
 * <li>In case Hibernate Validator is <em>not</em> the default provider, another pair of beans will be registered in
 * addition which are qualified with the {@code HibernateValidator} qualifier.</li>
 * </ul>
 * Neither of these beans will be registered in case there is already another bean with the same type and qualifier(s),
 * e.g. registered by another portable extension or the application itself.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class ValidationExtension implements Extension {

	private static final Log log = LoggerFactory.make( MethodHandles.lookup() );

	private static final EnumSet<ExecutableType> ALL_EXECUTABLE_TYPES =
			EnumSet.of( ExecutableType.CONSTRUCTORS, ExecutableType.NON_GETTER_METHODS, ExecutableType.GETTER_METHODS );
	private static final EnumSet<ExecutableType> DEFAULT_EXECUTABLE_TYPES =
			EnumSet.of( ExecutableType.CONSTRUCTORS, ExecutableType.NON_GETTER_METHODS );

	@SuppressWarnings("serial")
	private final Annotation defaultQualifier = new AnnotationLiteral<Default>() {
	};

	@SuppressWarnings("serial")
	private final Annotation hibernateValidatorQualifier = new AnnotationLiteral<HibernateValidator>() {
	};

	private final ExecutableHelper executableHelper;

	/**
	 * Used for identifying constrained classes
	 */
	private final Validator validator;
	private final ValidatorFactory validatorFactory;
	private final GetterPropertySelectionStrategyHelper getterPropertySelectionStrategyHelper;
	private final Set<ExecutableType> globalExecutableTypes;
	private final boolean isExecutableValidationEnabled;

	private Bean<?> defaultValidatorFactoryBean;
	private Bean<?> hibernateValidatorFactoryBean;

	private Bean<?> defaultValidatorBean;
	private Bean<?> hibernateValidatorBean;

	public ValidationExtension() {
		Configuration<?> config = Validation.byDefaultProvider().configure();
		// we use the default ParameterNameProvider here as we cannot use the injected one
		// as it hasn't been turned into a managed bean yet and might not be able to
		// return a parameter name. At this stage, we don't care about the parameter names.
		config.parameterNameProvider( config.getDefaultParameterNameProvider() );
		BootstrapConfiguration bootstrap = config.getBootstrapConfiguration();
		globalExecutableTypes = bootstrap.getDefaultValidatedExecutableTypes();
		isExecutableValidationEnabled = bootstrap.isExecutableValidationEnabled();
		validatorFactory = config.buildValidatorFactory();
		validator = validatorFactory.getValidator();
		getterPropertySelectionStrategyHelper = GetterPropertySelectionStrategyHelper.forValidationFactory( validatorFactory );

		executableHelper = new ExecutableHelper( new TypeResolutionHelper() );
	}

	/**
	 * Used to register the method validation interceptor binding annotation.
	 *
	 * @param beforeBeanDiscoveryEvent event fired before the bean discovery process starts
	 * @param beanManager the bean manager.
	 */
	public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscoveryEvent,
			final BeanManager beanManager) {
		Contracts.assertNotNull( beforeBeanDiscoveryEvent, "The BeforeBeanDiscovery event cannot be null" );
		Contracts.assertNotNull( beanManager, "The BeanManager cannot be null" );

		// Register the interceptor explicitly. This way, no beans.xml is needed
		AnnotatedType<ValidationInterceptor> annotatedType = beanManager.createAnnotatedType( ValidationInterceptor.class );
		beforeBeanDiscoveryEvent.addAnnotatedType( annotatedType, ValidationInterceptor.class.getName() );
	}

	/**
	 * Registers beans for {@code ValidatorFactory} and {@code Validator} if not yet present.
	 *
	 * @param afterBeanDiscoveryEvent event fired after the bean discovery phase.
	 * @param beanManager the bean manager.
	 */
	public void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscoveryEvent, BeanManager beanManager) {
		Contracts.assertNotNull( afterBeanDiscoveryEvent, "The AfterBeanDiscovery event cannot be null" );
		Contracts.assertNotNull( beanManager, "The BeanManager cannot be null" );

		ValidationProviderHelper defaultProviderHelper = ValidationProviderHelper.forDefaultProvider( validatorFactory );
		ValidationProviderHelper hvProviderHelper = ValidationProviderHelper.forHibernateValidator();

		// register default VF if none has been provided by the application or another PE
		if ( defaultValidatorFactoryBean == null ) {
			defaultValidatorFactoryBean = new ValidatorFactoryBean( beanManager, defaultProviderHelper );
			if ( hibernateValidatorFactoryBean == null && defaultProviderHelper.isHibernateValidator() ) {
				hibernateValidatorFactoryBean = defaultValidatorFactoryBean;
			}
			afterBeanDiscoveryEvent.addBean( defaultValidatorFactoryBean );
		}

		// register VF with @HibernateValidator qualifier in case it hasn't been contributed by the application and the
		// default VF registered by ourselves isn't for Hibernate Validator
		if ( hibernateValidatorFactoryBean == null ) {
			hibernateValidatorFactoryBean = new ValidatorFactoryBean( beanManager, hvProviderHelper );
			afterBeanDiscoveryEvent.addBean( hibernateValidatorFactoryBean );
		}

		// register default validator if required
		if ( defaultValidatorBean == null ) {
			defaultValidatorBean = new ValidatorBean( beanManager, defaultValidatorFactoryBean, defaultProviderHelper );
			if ( hibernateValidatorBean == null && defaultProviderHelper.isHibernateValidator() ) {
				hibernateValidatorBean = defaultValidatorBean;
			}
			afterBeanDiscoveryEvent.addBean( defaultValidatorBean );
		}

		// register validator with @HibernateValidator if required
		if ( hibernateValidatorBean == null ) {
			hibernateValidatorBean = new ValidatorBean( beanManager, hibernateValidatorFactoryBean, hvProviderHelper );
			afterBeanDiscoveryEvent.addBean( hibernateValidatorBean );
		}
	}

	/**
	 * Watches the {@code ProcessBean} event in order to determine whether beans for {@code ValidatorFactory} and
	 * {@code Validator} already have been registered by some other component.
	 *
	 * @param processBeanEvent event fired for each enabled bean.
	 */
	public void processBean(@Observes ProcessBean<?> processBeanEvent) {
		Contracts.assertNotNull( processBeanEvent, "The ProcessBean event cannot be null" );

		Bean<?> bean = processBeanEvent.getBean();

		if ( bean.getTypes().contains( ValidatorFactory.class ) || bean instanceof ValidatorFactoryBean ) {
			if ( bean.getQualifiers().contains( defaultQualifier ) ) {
				defaultValidatorFactoryBean = bean;
			}
			if ( bean.getQualifiers().contains( hibernateValidatorQualifier ) ) {
				hibernateValidatorFactoryBean = bean;
			}
		}
		else if ( bean.getTypes().contains( Validator.class ) || bean instanceof ValidatorBean ) {
			if ( bean.getQualifiers().contains( defaultQualifier ) ) {
				defaultValidatorBean = bean;
			}
			if ( bean.getQualifiers().contains( hibernateValidatorQualifier ) ) {
				hibernateValidatorBean = bean;
			}
		}
	}

	/**
	 * Used to register the method validation interceptor bindings.
	 *
	 * @param processAnnotatedTypeEvent event fired for each annotated type
	 * @param <T> the annotated type
	 */
	public <T> void processAnnotatedType(@Observes @WithAnnotations({
			Constraint.class,
			Valid.class,
			ValidateOnExecution.class
	}) ProcessAnnotatedType<T> processAnnotatedTypeEvent) {
		Contracts.assertNotNull( processAnnotatedTypeEvent, "The ProcessAnnotatedType event cannot be null" );

		// validation globally disabled
		if ( !isExecutableValidationEnabled ) {
			return;
		}

		AnnotatedType<T> type = processAnnotatedTypeEvent.getAnnotatedType();
		Set<AnnotatedCallable<? super T>> constrainedCallables = determineConstrainedCallables( type );

		if ( !constrainedCallables.isEmpty() ) {
			ValidationEnabledAnnotatedType<T> wrappedType = new ValidationEnabledAnnotatedType<>(
					type,
					constrainedCallables
			);
			processAnnotatedTypeEvent.setAnnotatedType( wrappedType );
		}
	}

	private <T> Set<AnnotatedCallable<? super T>> determineConstrainedCallables(AnnotatedType<T> type) {
		Set<AnnotatedCallable<? super T>> callables = new HashSet<>();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( type.getJavaClass() );

		determineConstrainedConstructors( type, beanDescriptor, callables );
		determineConstrainedMethods( type, beanDescriptor, callables );

		return callables;
	}

	private <T> void determineConstrainedMethods(AnnotatedType<T> type, BeanDescriptor beanDescriptor, Set<AnnotatedCallable<? super T>> callables) {
		List<Method> overriddenAndImplementedMethods = InheritedMethodsHelper.getAllMethods( type.getJavaClass() );

		for ( AnnotatedMethod<? super T> annotatedMethod : type.getMethods() ) {
			Method method = annotatedMethod.getJavaMember();

			Optional<String> correspondingProperty = getterPropertySelectionStrategyHelper.getProperty( method );

			// obtain @ValidateOnExecution from the top-most method in the hierarchy
			Method methodForExecutableTypeRetrieval = replaceWithOverriddenOrInterfaceMethod( method, overriddenAndImplementedMethods );

			EnumSet<ExecutableType> classLevelExecutableTypes = executableTypesDefinedOnType( methodForExecutableTypeRetrieval.getDeclaringClass() );
			EnumSet<ExecutableType> memberLevelExecutableType = executableTypesDefinedOnMethod( methodForExecutableTypeRetrieval,
					correspondingProperty.isPresent() );

			ExecutableType currentExecutableType = correspondingProperty.isPresent() ? ExecutableType.GETTER_METHODS : ExecutableType.NON_GETTER_METHODS;

			// validation is enabled per default, so explicit configuration can just veto whether
			// validation occurs
			if ( veto( classLevelExecutableTypes, memberLevelExecutableType, currentExecutableType ) ) {
				continue;
			}

			boolean needsValidation;
			if ( correspondingProperty.isPresent() ) {
				needsValidation = isGetterConstrained( beanDescriptor, method, correspondingProperty.get() );
			}
			else {
				needsValidation = isNonGetterConstrained( beanDescriptor, method );
			}

			if ( needsValidation ) {
				callables.add( annotatedMethod );
			}
		}
	}

	private <T> void determineConstrainedConstructors(AnnotatedType<T> type, BeanDescriptor beanDescriptor, Set<AnnotatedCallable<? super T>> callables) {
		Class<?> clazz = type.getJavaClass();
		EnumSet<ExecutableType> classLevelExecutableTypes = executableTypesDefinedOnType( clazz );

		for ( AnnotatedConstructor<T> annotatedConstructor : type.getConstructors() ) {
			Constructor<?> constructor = annotatedConstructor.getJavaMember();
			EnumSet<ExecutableType> memberLevelExecutableType = executableTypesDefinedOnConstructor( constructor );

			if ( veto( classLevelExecutableTypes, memberLevelExecutableType, ExecutableType.CONSTRUCTORS ) ) {
				continue;
			}

			if ( beanDescriptor.getConstraintsForConstructor( constructor.getParameterTypes() ) != null ) {
				callables.add( annotatedConstructor );
			}
		}
	}

	private boolean isNonGetterConstrained(BeanDescriptor beanDescriptor, Method method) {
		return beanDescriptor.getConstraintsForMethod( method.getName(), method.getParameterTypes() ) != null;
	}

	private boolean isGetterConstrained(BeanDescriptor beanDescriptor, Method method, String property) {
		PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty( property );
		return propertyDescriptor != null && propertyDescriptor.findConstraints()
				.declaredOn( ElementType.METHOD )
				.hasConstraints();
	}

	private boolean veto(EnumSet<ExecutableType> classLevelExecutableTypes,
			EnumSet<ExecutableType> memberLevelExecutableType,
			ExecutableType currentExecutableType) {
		if ( !memberLevelExecutableType.isEmpty() ) {
			return !memberLevelExecutableType.contains( currentExecutableType )
					&& !memberLevelExecutableType.contains( ExecutableType.IMPLICIT );
		}

		if ( !classLevelExecutableTypes.isEmpty() ) {
			return !classLevelExecutableTypes.contains( currentExecutableType )
					&& !classLevelExecutableTypes.contains( ExecutableType.IMPLICIT );
		}

		return !globalExecutableTypes.contains( currentExecutableType );
	}

	private EnumSet<ExecutableType> executableTypesDefinedOnType(Class<?> clazz) {
		ValidateOnExecution validateOnExecutionAnnotation = clazz.getAnnotation( ValidateOnExecution.class );
		EnumSet<ExecutableType> executableTypes = commonExecutableTypeChecks( validateOnExecutionAnnotation );

		if ( executableTypes.contains( ExecutableType.IMPLICIT ) ) {
			return DEFAULT_EXECUTABLE_TYPES;
		}

		return executableTypes;
	}

	private EnumSet<ExecutableType> executableTypesDefinedOnMethod(Method method, boolean isGetter) {
		ValidateOnExecution validateOnExecutionAnnotation = method.getAnnotation( ValidateOnExecution.class );
		EnumSet<ExecutableType> executableTypes = commonExecutableTypeChecks( validateOnExecutionAnnotation );

		if ( executableTypes.contains( ExecutableType.IMPLICIT ) ) {
			if ( isGetter ) {
				executableTypes.add( ExecutableType.GETTER_METHODS );
			}
			else {
				executableTypes.add( ExecutableType.NON_GETTER_METHODS );
			}
		}

		return executableTypes;
	}

	private EnumSet<ExecutableType> executableTypesDefinedOnConstructor(Constructor<?> constructor) {
		ValidateOnExecution validateOnExecutionAnnotation = constructor.getAnnotation(
				ValidateOnExecution.class
		);
		EnumSet<ExecutableType> executableTypes = commonExecutableTypeChecks( validateOnExecutionAnnotation );

		if ( executableTypes.contains( ExecutableType.IMPLICIT ) ) {
			executableTypes.add( ExecutableType.CONSTRUCTORS );
		}

		return executableTypes;
	}

	private EnumSet<ExecutableType> commonExecutableTypeChecks(ValidateOnExecution validateOnExecutionAnnotation) {
		if ( validateOnExecutionAnnotation == null ) {
			return EnumSet.noneOf( ExecutableType.class );
		}

		EnumSet<ExecutableType> executableTypes = EnumSet.noneOf( ExecutableType.class );
		if ( validateOnExecutionAnnotation.type().length == 0 ) {  // HV-757
			executableTypes.add( ExecutableType.NONE );
		}
		else {
			Collections.addAll( executableTypes, validateOnExecutionAnnotation.type() );
		}

		// IMPLICIT cannot be mixed 10.1.2 of spec - Mixing IMPLICIT and other executable types is illegal
		if ( executableTypes.contains( ExecutableType.IMPLICIT ) && executableTypes.size() > 1 ) {
			throw log.getMixingImplicitWithOtherExecutableTypesException();
		}

		// NONE can be removed 10.1.2 of spec - A list containing NONE and other types of executables is equivalent to a
		// list containing the types of executables without NONE.
		if ( executableTypes.contains( ExecutableType.NONE ) && executableTypes.size() > 1 ) {
			executableTypes.remove( ExecutableType.NONE );
		}

		// 10.1.2 of spec - A list containing ALL and other types of executables is equivalent to a list containing only ALL
		if ( executableTypes.contains( ExecutableType.ALL ) ) {
			executableTypes = ALL_EXECUTABLE_TYPES;
		}

		return executableTypes;
	}

	private Method replaceWithOverriddenOrInterfaceMethod(Method method, List<Method> allMethodsOfType) {
		LinkedList<Method> list = new LinkedList<>( allMethodsOfType );
		Iterator<Method> iterator = list.descendingIterator();
		while ( iterator.hasNext() ) {
			Method overriddenOrInterfaceMethod = iterator.next();
			if ( executableHelper.overrides( method, overriddenOrInterfaceMethod ) ) {
				if ( method.getAnnotation( ValidateOnExecution.class ) != null ) {
					throw log.getValidateOnExecutionOnOverriddenOrInterfaceMethodException( method );
				}
				return overriddenOrInterfaceMethod;
			}
		}

		return method;
	}
}
