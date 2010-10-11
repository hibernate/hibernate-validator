/*
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
package org.hibernate.validator.test.bootstrap;

import java.util.Set;
import javax.validation.Configuration;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.constraints.impl.NotNullValidator;
import org.hibernate.validator.engine.ConfigurationImpl;
import org.hibernate.validator.engine.ConstraintValidatorFactoryImpl;
import org.hibernate.validator.engine.ValidatorFactoryImpl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests the Bean Validation bootstrapping.
 *
 * @author Hardy Ferentschik
 */
public class ValidationTest {

	@Test
	public void testBootstrapAsServiceWithBuilder() {
		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();
		assertDefaultBuilderAndFactory( configuration );
	}

	@Test
	public void testBootstrapAsServiceDefault() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		assertDefaultFactory( factory );
	}

	@Test
	public void testCustomConstraintValidatorFactory() {

		Configuration<?> configuration = Validation.byDefaultProvider().configure();
		assertDefaultBuilderAndFactory( configuration );

		ValidatorFactory factory = configuration.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Customer customer = new Customer();
		customer.setFirstName( "John" );

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validate( customer );
		assertEquals( constraintViolations.size(), 1, "Wrong number of constraints" );
		ConstraintViolation<Customer> constraintViolation = constraintViolations.iterator().next();
		assertEquals( "may not be empty", constraintViolation.getMessage(), "Wrong message" );

		// get a new factory using a custom configuration
		configuration = Validation.byDefaultProvider().configure();
		configuration.constraintValidatorFactory(
				new ConstraintValidatorFactory() {

					public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
						if ( key == NotNullValidator.class ) {
							return ( T ) new BadlyBehavedNotNullConstraintValidator();
						}
						return new ConstraintValidatorFactoryImpl().getInstance( key );
					}
				}
		);
		factory = configuration.buildValidatorFactory();
		validator = factory.getValidator();
		constraintViolations = validator.validate( customer );
		assertEquals( constraintViolations.size(), 0, "Wrong number of constraints" );
	}

	/**
	 * HV-328
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullInputStream() {
		Configuration<?> configuration = Validation.byDefaultProvider().configure();
		configuration.addMapping( null );
		configuration.buildValidatorFactory();
	}

	private void assertDefaultBuilderAndFactory(Configuration configuration) {
		assertNotNull( configuration );
		assertTrue( configuration instanceof ConfigurationImpl );

		ValidatorFactory factory = configuration.buildValidatorFactory();
		assertDefaultFactory( factory );
	}

	private void assertDefaultFactory(ValidatorFactory factory) {
		assertNotNull( factory );
		assertTrue( factory instanceof ValidatorFactoryImpl );
	}

	class BadlyBehavedNotNullConstraintValidator extends NotNullValidator {
		@Override
		public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
			return true;
		}
	}
}
