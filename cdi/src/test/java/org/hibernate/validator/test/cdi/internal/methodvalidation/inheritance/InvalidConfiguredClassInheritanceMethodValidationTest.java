/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Inject;
import javax.validation.ValidationException;

import org.hibernate.validator.cdi.ValidationExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class InvalidConfiguredClassInheritanceMethodValidationTest extends Arquillian {

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
		try {
			extension.processAnnotatedType( new ProcessAnnotatedTypeImpl<CIA>( annotatedType ) );
			fail(
					"ValidationExtension should throw an exception, because the validated method overrides another " +
							"method and adds @ValidateOnExecution "
			);
		}
		catch (ValidationException e) {
			assertThat( e.getMessage() ).startsWith( "HV000166" );
		}
	}

	public static class ProcessAnnotatedTypeImpl<T> implements ProcessAnnotatedType<T> {
		private AnnotatedType<T> annotatedType;

		public ProcessAnnotatedTypeImpl(AnnotatedType<T> annotatedType) {
			this.annotatedType = annotatedType;
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
