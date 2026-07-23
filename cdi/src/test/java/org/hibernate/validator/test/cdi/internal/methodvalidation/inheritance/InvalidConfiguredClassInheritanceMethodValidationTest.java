/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;

import org.hibernate.validator.cdi.ValidationExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * @author Hardy Ferentschik
 */
@ExtendWith(ArquillianExtension.class)
public class InvalidConfiguredClassInheritanceMethodValidationTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addClass( CIA.class )
				.addClass( SecretServiceBase.class );
	}

	@Inject
	BeanManager beanManager;

	@Test
	public void testInvalidConfigurationThrowsException() {
		ValidationExtension extension = beanManager.getExtension( ValidationExtension.class );
		AnnotatedType<CIA> annotatedType = beanManager.createAnnotatedType( CIA.class );
		assertThatThrownBy( () -> extension.processAnnotatedType( new ProcessAnnotatedTypeImpl<CIA>( annotatedType ) ) )
				.isInstanceOf( ValidationException.class )
				.hasMessageStartingWith( "HV000166" );
	}

	public static class ProcessAnnotatedTypeImpl<T> implements ProcessAnnotatedType<T> {
		private AnnotatedType<T> annotatedType;

		public ProcessAnnotatedTypeImpl(AnnotatedType<T> annotatedType) {
			this.annotatedType = annotatedType;
		}

		@Override
		public AnnotatedTypeConfigurator<T> configureAnnotatedType() {
			// returning null as it's not relevant for our tests and returning a CDI 2.0 only class is out of the
			// question for now as we still support WildFly 11 and CDI 1.x.
			return null;
		}

		@Override
		public AnnotatedType<T> getAnnotatedType() {
			return annotatedType;
		}

		@Override
		public void setAnnotatedType(AnnotatedType<T> type) {
			this.annotatedType = type;
		}

		@Override
		public void veto() {
		}
	}
}
