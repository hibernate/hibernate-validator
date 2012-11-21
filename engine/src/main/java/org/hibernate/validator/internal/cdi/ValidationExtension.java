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
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.spi.MethodValidated;

import org.hibernate.validator.cdi.HibernateValidator;
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

	public void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscoveryEvent, BeanManager beanManager) {
		Contracts.assertNotNull( afterBeanDiscoveryEvent, "The AfterBeanDiscovery event cannot be null" );
		Contracts.assertNotNull( beanManager, "The BeanManager cannot be null" );

		addValidatorFactoryIfRequired( afterBeanDiscoveryEvent, beanManager );
		addValidatorIfRequired( afterBeanDiscoveryEvent, beanManager );
	}

	public <T> void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscoveryEvent, final BeanManager beanManager) {
		 beforeBeanDiscoveryEvent.addInterceptorBinding( MethodValidated.class );
	}

	public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
		final AnnotatedType<T> type = pat.getAnnotatedType();
		// TODO - add @MethodValidated programmatically where needed
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


