/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.provider;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Constraint;
import javax.validation.GroupSequence;

import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.jandex.ClassConstraintsJandexBuilder;
import org.hibernate.validator.internal.metadata.jandex.ConstrainedFieldJandexBuilder;
import org.hibernate.validator.internal.metadata.jandex.ConstrainedMethodJandexBuilder;
import org.hibernate.validator.internal.metadata.jandex.util.JandexHelper;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetMethods;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

/**
 * {@code MetaDataProvider} which reads the metadata from a Jandex index.
 *
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public class JandexMetaDataProvider implements MetaDataProvider {

	private static final Log LOG = LoggerFactory.make();

	private static final DotName CONSTRAINT_ANNOTATION = DotName.createSimple( Constraint.class.getName() );

	private final AnnotationProcessingOptions annotationProcessingOptions;

	private final Map<DotName, BeanConfiguration<?>> configuredBeans;

	public JandexMetaDataProvider(
			ConstraintHelper constraintHelper,
			JandexHelper jandexHelper,
			IndexView indexView,
			AnnotationProcessingOptions annotationProcessingOptions,
			ExecutableParameterNameProvider parameterNameProvider) {
		this.annotationProcessingOptions = annotationProcessingOptions;

		List<DotName> constraintAnnotations = Collections.unmodifiableList( extractConstraintAnnotations( indexView ) );

		this.configuredBeans = Collections.unmodifiableMap( extractConfiguredBeans( indexView,
				constraintHelper, jandexHelper,
				annotationProcessingOptions, parameterNameProvider,
				constraintAnnotations ) );
	}

	private static List<DotName> extractConstraintAnnotations(IndexView indexView) {
		return indexView.getAnnotations( CONSTRAINT_ANNOTATION ).stream()
				.filter( ai -> Kind.CLASS.equals( ai.target().kind() ) )
				.map( ai -> ai.target().asClass().name() )
				.collect( Collectors.toList() );
	}

	private static Map<DotName, BeanConfiguration<?>> extractConfiguredBeans(IndexView indexView, ConstraintHelper constraintHelper, JandexHelper jandexHelper,
			AnnotationProcessingOptions annotationProcessingOptions, ExecutableParameterNameProvider parameterNameProvider, List<DotName> constraintAnnotations) {
		return findConstrainedBeans( indexView, constraintAnnotations ).stream()
				.collect( Collectors.toMap(
						classInfo -> classInfo.name(),
						classInfo -> getBeanConfiguration(
								constraintHelper,
								jandexHelper,
								classInfo,
								annotationProcessingOptions,
								parameterNameProvider,
								constraintAnnotations
						)
				) );
	}

	private static Set<ClassInfo> findConstrainedBeans(IndexView indexView, List<DotName> constraintAnnotations) {
		// TODO HV-644: not sure this is totally accurate. I'm especially wondering how it will behave with subclasses and such.
		Set<ClassInfo> constrainedBeans = new HashSet<>();
		for ( DotName constraintAnnotation : constraintAnnotations ) {
			constrainedBeans.addAll( indexView.getAnnotations( constraintAnnotation ).stream()
					.map( ai -> ai.target() )
					.map( JandexMetaDataProvider::annotationInstanceToClassInfo )
					.collect( Collectors.toSet() ) );
		}
		return constrainedBeans;
	}

	private static ClassInfo annotationInstanceToClassInfo(AnnotationTarget target) {
		ClassInfo classInfo;
		switch ( target.kind() ) {
			case CLASS:
				classInfo = target.asClass();
				break;
			case FIELD:
				classInfo = target.asField().declaringClass();
				break;
			case METHOD:
				classInfo = target.asMethod().declaringClass();
				break;
			case METHOD_PARAMETER:
				classInfo = target.asMethodParameter().method().declaringClass();
				break;
			case TYPE:
				classInfo = annotationInstanceToClassInfo( target.asType().enclosingTarget() );
				break;
			default:
				throw new IllegalStateException( target.kind() + " is not supported here." );
		}
		return classInfo;
	}

	private static BeanConfiguration<?> getBeanConfiguration(ConstraintHelper constraintHelper, JandexHelper jandexHelper,
			ClassInfo classInfo, AnnotationProcessingOptions annotationProcessingOptions, ExecutableParameterNameProvider parameterNameProvider,
			List<DotName> constraintAnnotations) {
		Class<?> bean = jandexHelper.getClassForName( classInfo.name() );
		return new BeanConfiguration<>(
				ConfigurationSource.JANDEX,
				bean,
				getConstrainedElements( constraintHelper, jandexHelper, classInfo, bean, annotationProcessingOptions, parameterNameProvider, constraintAnnotations )
						.collect( Collectors.toSet() ),
				getGroupSequence( jandexHelper, classInfo ).collect( Collectors.toList() ),
				getGroupSequenceProvider( jandexHelper, classInfo )
		);
	}

	private static Stream<? extends ConstrainedElement> getConstrainedElements(
			ConstraintHelper constraintHelper,
			JandexHelper jandexHelper,
			ClassInfo classInfo,
			Class<?> bean,
			AnnotationProcessingOptions annotationProcessingOptions,
			ExecutableParameterNameProvider parameterNameProvider,
			List<DotName> constraintAnnotations
	) {
		// get constrained fields
		Stream<ConstrainedElement> constrainedFieldStream = new ConstrainedFieldJandexBuilder(
				constraintHelper,
				jandexHelper,
				annotationProcessingOptions,
				constraintAnnotations
		).getConstrainedFields( classInfo, bean );

		// get constrained methods/constructors
		Stream<ConstrainedElement> constrainedMethodStream = new ConstrainedMethodJandexBuilder(
				constraintHelper,
				jandexHelper,
				annotationProcessingOptions,
				parameterNameProvider,
				constraintAnnotations
		).getConstrainedExecutables( classInfo, bean );

		// get class level constraints
		Stream<ConstrainedElement> constrainedClassStream = new ClassConstraintsJandexBuilder(
				constraintHelper,
				jandexHelper,
				annotationProcessingOptions,
				constraintAnnotations
		).getClassConstraints( classInfo, bean );

		return Stream.of( constrainedClassStream, constrainedFieldStream, constrainedMethodStream )
				.reduce( Stream::concat )
				.orElse( Stream.empty() );
	}

	@Override
	public AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}

	@Override
	public <T> List<BeanConfiguration<? super T>> getBeanConfigurationForHierarchy(Class<T> beanClass) {
		List<BeanConfiguration<? super T>> configurations = newArrayList();

		for ( Class<? super T> clazz : ClassHierarchyHelper.getHierarchy( beanClass ) ) {
			BeanConfiguration<? super T> configuration = getBeanConfiguration( clazz );
			if ( configuration != null ) {
				configurations.add( configuration );
			}
		}

		return configurations;
	}

	@SuppressWarnings("unchecked")
	protected <T> BeanConfiguration<T> getBeanConfiguration(Class<T> beanClass) {
		Contracts.assertNotNull( beanClass );
		return (BeanConfiguration<T>) configuredBeans.get( DotName.createSimple( beanClass.getName() ) );
	}

	private static Stream<Class<?>> getGroupSequence(JandexHelper jandexHelper, ClassInfo classInfo) {
		Optional<AnnotationInstance> groupSequence = jandexHelper.findAnnotation( classInfo.classAnnotations(), GroupSequence.class );
		if ( groupSequence.isPresent() ) {
			return Arrays.stream( (Class<?>[]) jandexHelper.convertAnnotationValue( groupSequence.get().value() ) );
		}
		else {
			return Stream.empty();
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> DefaultGroupSequenceProvider<? super T> getGroupSequenceProvider(JandexHelper jandexHelper, ClassInfo classInfo) {
		Optional<AnnotationInstance> groupSequenceProvider = jandexHelper.findAnnotation( classInfo.classAnnotations(), GroupSequenceProvider.class );
		if ( groupSequenceProvider.isPresent() ) {
			Class<? extends DefaultGroupSequenceProvider<? super T>> providerClass =
					(Class<? extends DefaultGroupSequenceProvider<? super T>>) jandexHelper.convertAnnotationValue( groupSequenceProvider.get().value() );
			return newGroupSequenceProviderClassInstance( jandexHelper.getClassForName( classInfo.name() ), providerClass );
		}
		return null;
	}

	/**
	 * TODO: this method was directly copied from AnnotationMetaDataProvider, we will need to refactor this but it's
	 * better to wait for the dust to settle a bit
	 */
	private static <T> DefaultGroupSequenceProvider<? super T> newGroupSequenceProviderClassInstance(Class<?> beanClass, Class<? extends DefaultGroupSequenceProvider<? super T>> providerClass) {
		Method[] providerMethods = run( GetMethods.action( providerClass ) );
		for ( Method method : providerMethods ) {
			Class<?>[] paramTypes = method.getParameterTypes();
			if ( "getValidationGroups".equals( method.getName() ) && !method.isBridge()
					&& paramTypes.length == 1 && paramTypes[0].isAssignableFrom( beanClass ) ) {
				return run( NewInstance.action( providerClass, "the default group sequence provider" ) );
			}
		}
		throw LOG.getWrongDefaultGroupSequenceProviderTypeException( beanClass );
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}

}
