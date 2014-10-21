/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.cdi.methodvalidation.inheritance;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Inject;
import javax.validation.ValidationException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.hibernate.validator.internal.cdi.ValidationExtension;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
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
		try {
			extension.processAnnotatedType( new ProcessAnnotatedTypeImpl<CIA>( annotatedType ) );
			fail(
					"ValidationExtension should throw an exception, because the validated method overrides another " +
							"method and adds @ValidateOnExecution "
			);
		}
		catch ( ValidationException e ) {
			assertTrue( e.getMessage().startsWith( "HV000166" ) );
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
