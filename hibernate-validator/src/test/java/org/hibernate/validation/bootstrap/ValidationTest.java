// $Id: ValidationTest.java 115 2008-10-01 15:33:10Z hardy.ferentschik $
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
import javax.validation.Constraint;
import javax.validation.ConstraintDescriptor;
import javax.validation.ConstraintFactory;
import javax.validation.InvalidConstraint;
import javax.validation.MessageResolver;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.ValidationProviderResolver;
import javax.validation.Validator;
import javax.validation.ValidatorBuilder;
import javax.validation.ValidatorFactory;
import javax.validation.Context;
import javax.validation.bootstrap.SpecializedBuilderFactory;
import javax.validation.spi.ValidationProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

import org.hibernate.validation.HibernateValidatorBuilder;
import org.hibernate.validation.constraints.NotNullConstraint;
import org.hibernate.validation.eg.Customer;
import org.hibernate.validation.impl.ConstraintFactoryImpl;
import org.hibernate.validation.impl.ValidatorBuilderImpl;
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
		HibernateValidatorBuilder builder = Validation
				.builderType( HibernateValidatorBuilder.class )
				.getValidatorBuilder();
		assertDefaultBuilderAndFactory( builder );
	}

	@Test
	public void testBootstrapAsServiceDefault() {
		ValidatorBuilder<?> builder = Validation.getValidatorBuilder();
		assertDefaultBuilderAndFactory( builder );
	}

	@Test
	public void testGetCustomerValiator() {
		ValidatorBuilder<?> builder = Validation.getValidatorBuilder();
		assertDefaultBuilderAndFactory( builder );

		ValidatorFactory factory = builder.build();
		Validator<Customer> validator = factory.getValidator( Customer.class );

		Customer customer = new Customer();
		customer.setFirstName( "John" );

		Set<InvalidConstraint<Customer>> invalidConstraints = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 1, invalidConstraints.size() );

		customer.setLastName( "Doe" );

		invalidConstraints = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 0, invalidConstraints.size() );
	}


	@Test
	public void testCustomMessageResolver() {

		// first try with the default message resolver
		ValidatorBuilder<?> builder = Validation.getValidatorBuilder();
		assertDefaultBuilderAndFactory( builder );

		ValidatorFactory factory = builder.build();
		Validator<Customer> validator = factory.getValidator( Customer.class );

		Customer customer = new Customer();
		customer.setFirstName( "John" );

		Set<InvalidConstraint<Customer>> invalidConstraints = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 1, invalidConstraints.size() );
		InvalidConstraint<Customer> constraint = invalidConstraints.iterator().next();
		assertEquals( "Wrong message", "may not be null", constraint.getMessage() );

		//FIXME nothing guarantee that a builder can be reused
		// now we modify the builder, get a new factory and valiator and try again
		builder.messageResolver(
				new MessageResolver() {
					public String interpolate(String message, ConstraintDescriptor constraintDescriptor, Object value) {
						return "my custom message";
					}
				}
		);
		factory = builder.build();
		validator = factory.getValidator( Customer.class );
		invalidConstraints = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 1, invalidConstraints.size() );
		constraint = invalidConstraints.iterator().next();
		assertEquals( "Wrong message", "my custom message", constraint.getMessage() );
	}

	@Test
	public void testCustomConstraintFactory() {

		ValidatorBuilder<?> builder = Validation.getValidatorBuilder();
		assertDefaultBuilderAndFactory( builder );

		ValidatorFactory factory = builder.build();
		Validator<Customer> validator = factory.getValidator( Customer.class );

		Customer customer = new Customer();
		customer.setFirstName( "John" );

		Set<InvalidConstraint<Customer>> invalidConstraints = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 1, invalidConstraints.size() );
		InvalidConstraint<Customer> constraint = invalidConstraints.iterator().next();
		assertEquals( "Wrong message", "may not be null", constraint.getMessage() );

		//FIXME nothing guarantee that a builder can be reused
		// now we modify the builder, get a new factory and valiator and try again
		builder.constraintFactory(
				new ConstraintFactory() {
					public <T extends Constraint> T getInstance(Class<T> key) {
						if ( key == NotNullConstraint.class ) {
							return ( T ) new BadlyBehavedNotNullConstraint();
						}
						return new ConstraintFactoryImpl().getInstance( key );
					}
				}
		);
		factory = builder.build();
		validator = factory.getValidator( Customer.class );
		invalidConstraints = validator.validate( customer );
		assertEquals( "Wrong number of constraints", 0, invalidConstraints.size() );
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


		HibernateValidatorBuilder builder = Validation
					.builderType( HibernateValidatorBuilder.class )
					.providerResolver( resolver )
					.getValidatorBuilder();
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


		ValidatorBuilder<?> builder = Validation
			        .defineBootstrapState()
					.providerResolver( resolver )
					.getValidatorBuilder();
		assertDefaultBuilderAndFactory( builder );
	}

	@Test
	public void testFailingCustomResolver() {
		ValidationProviderResolver resolver = new ValidationProviderResolver() {

			public List<ValidationProvider> getValidationProviders() {
				return new ArrayList<ValidationProvider>();
			}
		};

		final SpecializedBuilderFactory<HibernateValidatorBuilder> specializedBuilderFactory =
				Validation
						.builderType(HibernateValidatorBuilder.class)
						.providerResolver( resolver );

		try {
			specializedBuilderFactory.getValidatorBuilder();
			fail();
		}
		catch ( ValidationException e ) {
			assertEquals(
					"Wrong error message",
					"Unable to find provider: interface org.hibernate.validation.HibernateValidatorBuilder",
					e.getMessage()
			);
		}
	}

	private void assertDefaultBuilderAndFactory(ValidatorBuilder builder) {
		assertNotNull( builder );
		assertTrue( builder instanceof ValidatorBuilderImpl );

		ValidatorFactory factory = builder.build();
		assertNotNull( factory );
		assertTrue( factory instanceof ValidatorFactoryImpl );
	}

	class BadlyBehavedNotNullConstraint extends NotNullConstraint {
		@Override
		public boolean isValid(Object object, Context context) {
			return true;
		}
	}
}
