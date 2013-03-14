/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.cdi;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.enterprise.util.AnnotationLiteral;
import javax.validation.BootstrapConfiguration;
import javax.validation.Configuration;
import javax.validation.Constraint;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateExecutable;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.internal.cdi.interceptor.ValidationEnabledAnnotatedType;
import org.hibernate.validator.internal.cdi.interceptor.ValidationInterceptor;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ReflectionHelper;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * A CDI portable extension which registers beans for {@link ValidatorFactory} and {@link Validator},
 * if such beans not yet exist (which for instance would be the case in a Java EE 6 container).
 * All registered beans will be {@link ApplicationScoped}.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class ValidationExtension implements Extension {
	private static final EnumSet<ExecutableType> ALL_EXECUTABLE_TYPES =
			EnumSet.of( ExecutableType.CONSTRUCTORS, ExecutableType.NON_GETTER_METHODS, ExecutableType.GETTER_METHODS );

	private final Validator validator;
	private final Set<ExecutableType> globalExecutableTypes;
	private boolean validatorRegisteredUnderDefaultQualifier;
	private boolean validatorRegisteredUnderHibernateQualifier;

	public ValidationExtension() {
		validatorRegisteredUnderDefaultQualifier = false;
		validatorRegisteredUnderHibernateQualifier = false;

		Configuration<?> config = Validation.byDefaultProvider().configure();
		BootstrapConfiguration bootstrap = config.getBootstrapConfiguration();
		globalExecutableTypes = bootstrap.getValidatedExecutableTypes();
		validator = config.buildValidatorFactory().getValidator();
	}

	/**
	 * Used to register the method validation interceptor binding annotation.
	 *
	 * @param beforeBeanDiscoveryEvent event fired before the bean discovery process starts
	 * @param beanManager the bean manager.
	 */
	public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscoveryEvent, final BeanManager beanManager) {
		Contracts.assertNotNull( beforeBeanDiscoveryEvent, "The BeforeBeanDiscovery event cannot be null" );
		Contracts.assertNotNull( beanManager, "The BeanManager cannot be null" );

		// Register the interceptor explicitly. This way, no beans.xml is needed
		AnnotatedType<ValidationInterceptor> annotatedType = beanManager.createAnnotatedType( ValidationInterceptor.class );
		beforeBeanDiscoveryEvent.addAnnotatedType( annotatedType );
	}

	/**
	 * Registers the Hibernate specific {@code ValidatorFactory} and {@code Validator}. The qualifiers used for registration
	 * depend on which other beans have already registered these type of beans.
	 *
	 * @param afterBeanDiscoveryEvent event fired after the bean discovery phase.
	 * @param beanManager the bean manager.
	 */
	public void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscoveryEvent, BeanManager beanManager) {
		Contracts.assertNotNull( afterBeanDiscoveryEvent, "The AfterBeanDiscovery event cannot be null" );
		Contracts.assertNotNull( beanManager, "The BeanManager cannot be null" );

		Set<Annotation> missingQualifiers = determineMissingQualifiers();
		if ( missingQualifiers.isEmpty() ) {
			return;
		}

		afterBeanDiscoveryEvent.addBean( new ValidatorFactoryBean( beanManager, missingQualifiers ) );
		afterBeanDiscoveryEvent.addBean( new ValidatorBean( beanManager, missingQualifiers ) );
	}

	/**
	 * Watches the {@code ProcessBean} event in order to determine whether and under which qualifiers {@code ValidatorFactory}s
	 * and {@code Validator}s get registered.
	 *
	 * @param processBeanEvent event fired for each enabled bean.
	 */
	public void processBean(@Observes ProcessBean<?> processBeanEvent) {
		Contracts.assertNotNull( processBeanEvent, "The ProcessBean event cannot be null" );

		Bean<?> bean = processBeanEvent.getBean();
		if ( !bean.getTypes().contains( ValidatorFactory.class ) && !bean.getTypes().contains( Validator.class ) ) {
			return;
		}
		if ( bean instanceof ValidatorFactoryBean || bean instanceof ValidatorBean ) {
			return;
		}
		for ( Annotation annotation : bean.getQualifiers() ) {
			if ( HibernateValidator.class.equals( annotation.annotationType() ) ) {
				validatorRegisteredUnderHibernateQualifier = true;
			}
			if ( Default.class.equals( annotation.annotationType() ) ) {
				validatorRegisteredUnderDefaultQualifier = true;
			}
		}
	}

	/**
	 * Used to register the method validation interceptor bindings.
	 *
	 * @param processAnnotatedTypeEvent event fired for each annotated type
	 */
	public <T> void processAnnotatedType(@Observes @WithAnnotations({ Constraint.class, ValidateExecutable.class })
										 ProcessAnnotatedType<T> processAnnotatedTypeEvent) {
		Contracts.assertNotNull( processAnnotatedTypeEvent, "The ProcessAnnotatedType event cannot be null" );

		final AnnotatedType<T> type = processAnnotatedTypeEvent.getAnnotatedType();

		EnumSet<ExecutableType> classLevelExecutableTypes = executableTypesForAnnotatedElement( type );

		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( type.getJavaClass() );
		if ( !beanDescriptor.hasConstrainedExecutables() ) {
			return;
		}

		Set<AnnotatedCallable<? super T>> constrainedCallables = determineConstrainedCallables(
				type,
				beanDescriptor,
				classLevelExecutableTypes
		);
		if ( !constrainedCallables.isEmpty() ) {
			ValidationEnabledAnnotatedType<T> wrappedType = new ValidationEnabledAnnotatedType<T>(
					type,
					constrainedCallables
			);
			processAnnotatedTypeEvent.setAnnotatedType( wrappedType );
		}
	}

	private <T> Set<AnnotatedCallable<? super T>> determineConstrainedCallables(AnnotatedType<T> type,
																				BeanDescriptor beanDescriptor,
																				EnumSet<ExecutableType> classLevelExecutableTypes) {
		Set<AnnotatedCallable<? super T>> callables = newHashSet();

		for ( AnnotatedConstructor<T> annotatedConstructor : type.getConstructors() ) {
			Constructor constructor = annotatedConstructor.getJavaMember();
			EnumSet<ExecutableType> memberLevelExecutableType = executableTypesForAnnotatedElement( annotatedConstructor );

			if ( veto( classLevelExecutableTypes, memberLevelExecutableType, ExecutableType.CONSTRUCTORS ) ) {
				continue;
			}

			if ( beanDescriptor.getConstraintsForConstructor( constructor.getParameterTypes() ) != null ) {
				callables.add( annotatedConstructor );
			}
		}

		for ( AnnotatedMethod<? super T> annotatedMethod : type.getMethods() ) {
			Method method = annotatedMethod.getJavaMember();
			EnumSet<ExecutableType> memberLevelExecutableType = executableTypesForAnnotatedElement( annotatedMethod );
			boolean isGetter = ReflectionHelper.isGetterMethod( method );
			ExecutableType currentExecutableType = isGetter ? ExecutableType.GETTER_METHODS : ExecutableType.NON_GETTER_METHODS;

			// validation is enabled per default, so explicit configuration can just veto whether
			// validation occurs
			if ( veto( classLevelExecutableTypes, memberLevelExecutableType, currentExecutableType ) ) {
				continue;
			}

			boolean needsValidation;
			if ( isGetter ) {
				needsValidation = isGetterConstrained( method, beanDescriptor );

			}
			else {
				needsValidation = isNonGetterConstrained( method, beanDescriptor );
			}

			if ( needsValidation ) {
				callables.add( annotatedMethod );
			}
		}

		return callables;
	}

	private boolean isNonGetterConstrained(Method method, BeanDescriptor beanDescriptor) {
		return beanDescriptor.getConstraintsForMethod( method.getName(), method.getParameterTypes() ) != null;
	}

	private boolean isGetterConstrained(Method method, BeanDescriptor beanDescriptor) {
		String propertyName = ReflectionHelper.getPropertyName( method );
		PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty( propertyName );
		return propertyDescriptor.findConstraints()
				.declaredOn( ElementType.METHOD )
				.hasConstraints();
	}

	private boolean veto(EnumSet<ExecutableType> classLevelExecutableTypes,
						 EnumSet<ExecutableType> memberLevelExecutableType,
						 ExecutableType currentExecutableType) {
		if ( !memberLevelExecutableType.isEmpty() ) {
			return !memberLevelExecutableType.contains( currentExecutableType );
		}

		if ( !classLevelExecutableTypes.isEmpty() ) {
			return !classLevelExecutableTypes.contains( currentExecutableType );
		}

		return !globalExecutableTypes.contains( currentExecutableType );
	}

	/**
	 * The set of qualifier this extension should bind its {@code ValidatorFactory} and {@code Validator}
	 * under. This set is based on the observed registered beans via the {@code ProcessBean} event.
	 *
	 * @return Returns the set of qualifier this extension should bind its {@code ValidatorFactory} and {@code Validator}
	 *         under.
	 */
	private Set<Annotation> determineMissingQualifiers() {
		Set<Annotation> annotations = newHashSet( 2 );

		if ( !validatorRegisteredUnderDefaultQualifier ) {
			annotations.add(
					new AnnotationLiteral<Default>() {
					}
			);
		}

		if ( !validatorRegisteredUnderHibernateQualifier ) {
			annotations.add(
					new AnnotationLiteral<HibernateValidator>() {
					}
			);
		}
		return annotations;
	}

	private EnumSet<ExecutableType> executableTypesForAnnotatedElement(Annotated annotated) {
		if ( !annotated.isAnnotationPresent( ValidateExecutable.class ) ) {
			return EnumSet.noneOf( ExecutableType.class );
		}

		ValidateExecutable executableAnnotation = annotated.getAnnotation( ValidateExecutable.class );

		EnumSet<ExecutableType> executableTypes = EnumSet.noneOf( ExecutableType.class );
		Collections.addAll( executableTypes, executableAnnotation.value() );

		if ( executableTypes.contains( ExecutableType.ALL ) ) {
			return ALL_EXECUTABLE_TYPES;
		}

		if ( executableTypes.contains( ExecutableType.NONE ) && executableTypes.size() > 1 ) {
			executableTypes.remove( ExecutableType.NONE );
		}

		return executableTypes;
	}
}
