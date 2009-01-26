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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Locale;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintDescriptor;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.ValidationProviderResolver;
import javax.validation.Validator;
import javax.validation.ValidatorFactoryBuilder;
import javax.validation.ValidatorFactory;
import javax.validation.ConstraintValidatorContext;
import javax.validation.bootstrap.SpecializedBuilderFactory;
import javax.validation.spi.ValidationProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

import org.hibernate.validation.HibernateValidatorFactoryBuilder;
import org.hibernate.validation.constraints.NotNullConstraintValidator;
import org.hibernate.validation.eg.Customer;
import org.hibernate.validation.impl.ConstraintValidatorFactoryImpl;
import org.hibernate.validation.impl.ValidatorFactoryBuilderImpl;
import org.hibernate.validation.impl.ValidatorFactoryImpl;
import org.hibernate.validation.impl.HibernateValidationProvider;

/**
 * Tests the validation bootstrapping.
 *
 * @author Hardy Ferentschik
 */
public class ValidationTest {

	@Test
	public void testBootstrapAsServiceWithBuilder() {
		HibernateValidatorFactoryBuilder builder = Validation
				.builderType( HibernateValidatorFactoryBuilder.class )
				.getBuilder();
		assertDefaultBuilderAndFactory( builder );
	}

	@Test
	public void testBootstrapAsServiceDefault() {
		ValidatorFactoryBuilder<?> builder = Validation.getBuilder();
		assertDefaultBuilderAndFactory( builder );
	}

	@Test
	public void testGetCustomerValiator() {
		ValidatorFactoryBuilder<?> builder = Validation.getBuilder();
		assertDefaultBuilderAndFactory( builder );

		ValidatorFactory factory = builder.build();
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
		ValidatorFactoryBuilder<?> builder = Validation.getBuilder();
		assertDefaultBuilderAndFactory( builder );

		ValidatorFactory factory = builder.build();
		Validator validator = factory.getValidator( );

		Customer customer = new Customer();
		customer.setFirstName( "John" );

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		ConstraintViolation<Customer> constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong message", "may not be null", constraintViolation.getInterpolatedMessage() );

		//FIXME nothing guarantee that a builder can be reused
		// now we modify the builder, get a new factory and valiator and try again
		builder.messageInterpolator(
				new MessageInterpolator() {
					public String interpolate(String message, ConstraintDescriptor constraintDescriptor, Object value) {
						return "my custom message";
					}

					public String interpolate(String message, ConstraintDescriptor constraintDescriptor, Object value, Locale locale) {
						throw new UnsupportedOperationException( "No specific locale is possible" );
					}
				}
		);
		factory = builder.build();
		validator = factory.getValidator( );
		constraintViolations = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong message", "my custom message", constraintViolation.getInterpolatedMessage() );
	}

	@Test
	public void testCustomConstraintValidatorFactory() {

		ValidatorFactoryBuilder<?> builder = Validation.getBuilder();
		assertDefaultBuilderAndFactory( builder );

		ValidatorFactory factory = builder.build();
		Validator validator = factory.getValidator(  );

		Customer customer = new Customer();
		customer.setFirstName( "John" );

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		ConstraintViolation<Customer> constraintViolation = constraintViolations.iterator().next();
		assertEquals( "Wrong message", "may not be null", constraintViolation.getInterpolatedMessage() );

		//FIXME nothing guarantee that a builder can be reused
		// now we modify the builder, get a new factory and valiator and try again
		builder.constraintValidatorFactory(
				new ConstraintValidatorFactory() {
					public <T extends ConstraintValidator> T getInstance(Class<T> key) {
						if ( key == NotNullConstraintValidator.class ) {
							return ( T ) new BadlyBehavedNotNullConstraintValidator();
						}
						return new ConstraintValidatorFactoryImpl().getInstance( key );
					}
				}
		);
		factory = builder.build();
		validator = factory.getValidator( );
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


		HibernateValidatorFactoryBuilder builder = Validation
					.builderType( HibernateValidatorFactoryBuilder.class )
					.providerResolver( resolver )
					.getBuilder();
		assertDefaultBuilderAndFactory( builder );
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


		ValidatorFactoryBuilder<?> builder = Validation
			        .defineBootstrapState()
					.providerResolver( resolver )
					.getBuilder();
		assertDefaultBuilderAndFactory( builder );
	}

	@Test
	public void testFailingCustomResolver() {
		ValidationProviderResolver resolver = new ValidationProviderResolver() {

			public List<ValidationProvider> getValidationProviders() {
				return new ArrayList<ValidationProvider>();
			}
		};

		final SpecializedBuilderFactory<HibernateValidatorFactoryBuilder> specializedBuilderFactory =
				Validation
						.builderType( HibernateValidatorFactoryBuilder.class)
						.providerResolver( resolver );

		try {
			specializedBuilderFactory.getBuilder();
			fail();
		}
		catch ( ValidationException e ) {
			assertEquals(
					"Wrong error message",
					"Unable to find provider: interface org.hibernate.validation.HibernateValidatorFactoryBuilder",
					e.getMessage()
			);
		}
	}

	private void assertDefaultBuilderAndFactory(ValidatorFactoryBuilder builder) {
		assertNotNull( builder );
		assertTrue( builder instanceof ValidatorFactoryBuilderImpl );

		ValidatorFactory factory = builder.build();
		assertNotNull( factory );
		assertTrue( factory instanceof ValidatorFactoryImpl );
	}

	class BadlyBehavedNotNullConstraintValidator extends NotNullConstraintValidator {
		@Override
		public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
			return true;
		}
	}
}
