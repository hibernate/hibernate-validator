/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

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
			fail( "ValidationExtension should throw an exception, because the validated method overrides another " +
					"method and adds @ValidateOnExecution " );
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
