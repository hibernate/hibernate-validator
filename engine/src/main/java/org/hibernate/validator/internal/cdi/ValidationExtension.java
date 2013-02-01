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
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.internal.cdi.interceptor.MethodValidated;
import org.hibernate.validator.internal.cdi.interceptor.ValidationEnabledAnnotatedType;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.util.Contracts;

/**
 * A CDI portable extension which registers beans for {@link ValidatorFactory} and {@link javax.xml.validation.Validator},
 * if such beans not yet exist (which for instance would be the case in a Java EE 6 container).
 * All registered beans will be {@link ApplicationScoped}.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class ValidationExtension implements Extension {
	private final ConstraintHelper constraintHelper = new ConstraintHelper();

	public void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscoveryEvent, BeanManager beanManager) {
		Contracts.assertNotNull( afterBeanDiscoveryEvent, "The AfterBeanDiscovery event cannot be null" );
		Contracts.assertNotNull( beanManager, "The BeanManager cannot be null" );

		addValidatorFactoryIfRequired( afterBeanDiscoveryEvent, beanManager );
		addValidatorIfRequired( afterBeanDiscoveryEvent, beanManager );
	}

	public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscoveryEvent, final BeanManager beanManager) {
		Contracts.assertNotNull( beforeBeanDiscoveryEvent, "The BeforeBeanDiscovery event cannot be null" );
		beforeBeanDiscoveryEvent.addInterceptorBinding( MethodValidated.class );
	}

	public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> processAnnotatedTypeEvent) {
		Contracts.assertNotNull( processAnnotatedTypeEvent, "The ProcessAnnotatedType event cannot be null" );
		final AnnotatedType<T> type = processAnnotatedTypeEvent.getAnnotatedType();
		Set<AnnotatedCallable<T>> constrainedCallables = determineConstrainedCallables( type );
		if ( !constrainedCallables.isEmpty() ) {
			ValidationEnabledAnnotatedType<T> wrappedType = new ValidationEnabledAnnotatedType<T>(
					type,
					constrainedCallables
			);
			processAnnotatedTypeEvent.setAnnotatedType( wrappedType );
		}
	}

	private <T> Set<AnnotatedCallable<T>> determineConstrainedCallables(AnnotatedType<T> type) {
		Set<AnnotatedCallable<T>> callables = new HashSet<AnnotatedCallable<T>>();

		for ( AnnotatedConstructor<T> constructor : type.getConstructors() ) {
			if ( isCallableConstrained( constructor ) ) {
				callables.add( constructor );
			}
		}

		for ( AnnotatedMethod<? super T> method : type.getMethods() ) {
			if ( isCallableConstrained( method ) ) {
				callables.add( (AnnotatedMethod<T>) method );
			}
		}

		return callables;
	}

	private <T> boolean isCallableConstrained(AnnotatedCallable<? super T> callable) {
		if ( containsConstraintAnnotation( callable.getAnnotations() ) ) {
			return true;
		}

		for ( AnnotatedParameter<? super T> parameter : callable.getParameters() ) {
			if ( containsConstraintAnnotation( parameter.getAnnotations() ) ) {
				return true;
			}
		}

		return false;
	}

	private boolean containsConstraintAnnotation(Set<Annotation> annotations) {
		for ( Annotation annotation : annotations ) {
			if ( constraintHelper.isConstraintAnnotation( annotation.annotationType() ) ) {
				return true;
			}
		}
		return false;
	}

	private void addValidatorFactoryIfRequired(AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) {
		Set<Annotation> missingQualifiers = determineMissingQualifiers(
				beanManager,
				ValidatorFactory.class
		);
		if ( missingQualifiers.isEmpty() ) {
			return;
		}

		afterBeanDiscovery.addBean( new ValidatorFactoryBean( beanManager, missingQualifiers ) );
	}

	private void addValidatorIfRequired(AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) {
		Set<Annotation> missingQualifiers = determineMissingQualifiers( beanManager, Validator.class );
		if ( missingQualifiers.isEmpty() ) {
			return;
		}

		afterBeanDiscovery.addBean( new ValidatorBean( beanManager, missingQualifiers ) );
	}

	private Set<Annotation> determineMissingQualifiers(BeanManager beanManager, Class<?> clazz) {
		Set<Annotation> annotations = new HashSet<Annotation>( 2 );
		Set<Bean<?>> beans = beanManager.getBeans( clazz );
		boolean containsDefaultQualifier = false;
		boolean containsHibernateQualifier = false;
		for ( Bean<?> bean : beans ) {
			for ( Annotation annotation : bean.getQualifiers() ) {
				if ( HibernateValidator.class.equals( annotation.annotationType() ) ) {
					containsHibernateQualifier = true;
				}
				if ( Default.class.equals( annotation.annotationType() ) ) {
					containsDefaultQualifier = true;
				}
			}
		}

		if ( !containsDefaultQualifier ) {
			annotations.add(
					new AnnotationLiteral<Default>() {
					}
			);
		}

		if ( !containsHibernateQualifier ) {
			annotations.add(
					new AnnotationLiteral<HibernateValidator>() {
					}
			);
		}
		return annotations;
	}
}


