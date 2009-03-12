// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.bootstrap;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.validation.Configuration;
import javax.validation.ConstraintDescriptor;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.ValidationProviderResolver;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.bootstrap.ProviderSpecificBootstrap;
import javax.validation.spi.ValidationProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

import org.hibernate.validation.HibernateValidationProvider;
import org.hibernate.validation.constraints.NotNullValidator;
import org.hibernate.validation.eg.Customer;
import org.hibernate.validation.engine.ConfigurationImpl;
import org.hibernate.validation.engine.ConstraintValidatorFactoryImpl;
import org.hibernate.validation.engine.HibernateValidatorConfiguration;
import org.hibernate.validation.engine.ValidatorFactoryImpl;

/**
 * Tests the Bean Validation bootstrapping.
 *
 * @author Hardy Ferentschik
 */
public class ValidationTest {

	@Test
	public void testBootstrapAsServiceWithBuilder() {
		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidatorConfiguration.class )
				.configure();
		assertDefaultBuilderAndFactory( configuration );
	}

	@Test
	public void testBootstrapAsServiceDefault() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		assertDefaultFactory( factory );
	}

	@Test
	public void testGetCustomerValidator() {
		Configuration<?> configuration = Validation.byDefaultProvider().configure();
		assertDefaultBuilderAndFactory( configuration );

		ValidatorFactory factory = configuration.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Customer customer = new Customer();
		customer.setFirstName( "John" );

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );

		customer.setLastName( "Doe" );

		constraintViolations = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
	}

	@Test
	public void testCustomMessageInterpolator() {

		// first try with the default message resolver
		Configuration<?> configuration = Validation.byDefaultProvider().configure();
		assertDefaultBuilderAndFactory( configuration );

		ValidatorFactory factory = configuration.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Customer customer = new Customer();
		customer.setFirstName( "John" );

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		ConstraintViolation<Customer> constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong message", "may not be null", constraintViolation.getMessage() );

		// now we modify the configuration, get a new factory and valiator and try again
		configuration = Validation.byDefaultProvider().configure();
		configuration.messageInterpolator(
				new MessageInterpolator() {
					public String interpolate(String message, Context context) {
						return "my custom message";
					}

					public String interpolate(String message, Context context, Locale locale) {
						throw new UnsupportedOperationException( "No specific locale is possible" );
					}
				}
		);
		factory = configuration.buildValidatorFactory();
		validator = factory.getValidator();
		constraintViolations = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong message", "my custom message", constraintViolation.getMessage() );
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
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		ConstraintViolation<Customer> constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong message", "may not be null", constraintViolation.getMessage() );

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
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
	}

	@Test
	public void testCustomResolverAndType() {
		ValidationProviderResolver resolver = new ValidationProviderResolver() {

			public List<ValidationProvider> getValidationProviders() {
				List<ValidationProvider> list = new ArrayList<ValidationProvider>();
				list.add( new HibernateValidationProvider() );
				return list;
			}
		};


		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidatorConfiguration.class )
				.providerResolver( resolver )
				.configure();
		assertDefaultBuilderAndFactory( configuration );
	}

	@Test
	public void testCustomResolver() {
		ValidationProviderResolver resolver = new ValidationProviderResolver() {

			public List<ValidationProvider> getValidationProviders() {
				List<ValidationProvider> list = new ArrayList<ValidationProvider>();
				list.add( new HibernateValidationProvider() );
				return list;
			}
		};


		Configuration<?> configuration = Validation
				.byDefaultProvider()
				.providerResolver( resolver )
				.configure();
		assertDefaultBuilderAndFactory( configuration );
	}

	@Test
	public void testFailingCustomResolver() {
		ValidationProviderResolver resolver = new ValidationProviderResolver() {

			public List<ValidationProvider> getValidationProviders() {
				return new ArrayList<ValidationProvider>();
			}
		};

		final ProviderSpecificBootstrap<HibernateValidatorConfiguration> providerSpecificBootstrap =
				Validation
						.byProvider( HibernateValidatorConfiguration.class )
						.providerResolver( resolver );

		try {
			providerSpecificBootstrap.configure();
			fail();
		}
		catch ( ValidationException e ) {
			assertEquals(
					"Wrong error message",
					"Unable to find provider: interface org.hibernate.validation.engine.HibernateValidatorConfiguration",
					e.getMessage()
			);
		}
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

	@Test
	public void testCustomTraversableResolver() {

		Configuration<?> configuration = Validation.byDefaultProvider().configure();
		assertDefaultBuilderAndFactory( configuration );

		ValidatorFactory factory = configuration.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Customer customer = new Customer();
		customer.setFirstName( "John" );

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		ConstraintViolation<Customer> constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong message", "may not be null", constraintViolation.getMessage() );

		// get a new factory using a custom configuration
		configuration = Validation.byDefaultProvider().configure();
		configuration.traversableResolver(
				new TraversableResolver() {
					public boolean isTraversable(Object o, String s, Class<?> aClass, String s1, ElementType elementType) {
						return false;
					}
				}
		);
		factory = configuration.buildValidatorFactory();
		validator = factory.getValidator();
		constraintViolations = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
	}
}
