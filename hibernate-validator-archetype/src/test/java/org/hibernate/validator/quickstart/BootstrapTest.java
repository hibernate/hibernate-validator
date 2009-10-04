/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.quickstart;

import java.lang.annotation.ElementType;
import java.util.Locale;
import javax.validation.Configuration;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;

/**
 * A module test that shows the different bootstrap possibilities of Hibernate Validator.
 *
 * @author Hardy Ferentschik
 */
public class BootstrapTest {

	@Test
	public void testBuildDefaultValidatorFactory() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		assertNotNull( validator );
	}

	@Test
	public void testByDefaultProvider() {
		Configuration<?> config = Validation.byDefaultProvider().configure();
		config.messageInterpolator( new MyMessageInterpolator() )
				.traversableResolver( new MyTraversableResolver() )
				.constraintValidatorFactory( new MyConstraintValidatorFactory() );

		ValidatorFactory factory = config.buildValidatorFactory();
		Validator validator = factory.getValidator();

		assertNotNull( validator );
	}

	@Test
	public void testByProvider() {
		HibernateValidatorConfiguration config = Validation.byProvider( HibernateValidator.class ).configure();
		config.messageInterpolator( new MyMessageInterpolator() )
				.traversableResolver( new MyTraversableResolver() )
				.constraintValidatorFactory( new MyConstraintValidatorFactory() );

		ValidatorFactory factory = config.buildValidatorFactory();
		Validator validator = factory.getValidator();

		assertNotNull( validator );
	}

	public class MyMessageInterpolator implements MessageInterpolator {

		public String interpolate(String messageTemplate, Context context) {
			return null;
		}

		public String interpolate(String messageTemplate, Context context, Locale locale) {
			return null;
		}
	}

	public class MyTraversableResolver implements TraversableResolver {

		public boolean isReachable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
			return true;
		}

		public boolean isCascadable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
			return true;
		}
	}

	public class MyConstraintValidatorFactory implements ConstraintValidatorFactory {

		public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
			return null;
		}
	}
}
