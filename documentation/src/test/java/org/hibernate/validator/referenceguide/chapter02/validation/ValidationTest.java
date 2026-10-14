/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter02.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;

import org.junit.BeforeClass;
import org.junit.Test;

public class ValidationTest {

	private static Validator validator;

	@BeforeClass
	public static void setUpValidator() {
		//tag::setUpValidator[]
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
		//end::setUpValidator[]
	}

	@Test
	public void validate() {
		//tag::validate[]
		Car car = new Car( null, true );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "must not be null", constraintViolations.iterator().next().getMessage() );
		//end::validate[]
	}

	@Test
	public void validateProperty() {
		//tag::validateProperty[]
		Car car = new Car( null, true );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validateProperty(
				car,
				"manufacturer"
		);

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "must not be null", constraintViolations.iterator().next().getMessage() );
		//end::validateProperty[]
	}

	@Test
	public void validateValue() {
		//tag::validateValue[]
		Set<ConstraintViolation<Car>> constraintViolations = validator.validateValue(
				Car.class,
				"manufacturer",
				null
		);

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "must not be null", constraintViolations.iterator().next().getMessage() );
		//end::validateValue[]
	}

	@Test
	public void nestedPropertyWithValid() {
		//tag::nestedPropertyWithValid[]
		Customer customer = new Customer();

		assertTrue( validator.validateProperty( customer, "address" ).isEmpty() );

		Set<ConstraintViolation<Customer>> violations = validator.validateProperty( customer, "address.city" );
		assertEquals( 1, violations.size() );
		assertSame( customer, violations.iterator().next().getRootBean() );
		assertEquals( "address.city", violations.iterator().next().getPropertyPath().toString() );
		//end::nestedPropertyWithValid[]
	}

	@Test
	public void nestedPropertyWithoutValid() {
		//tag::nestedPropertyWithoutValid[]
		CustomerWithoutValid customer = new CustomerWithoutValid();

		assertThrows( IllegalArgumentException.class,
				() -> validator.validateProperty( customer, "address.city" ) );

		Set<ConstraintViolation<Address>> violations = validator.validateProperty( customer.address, "city" );
		assertEquals( 1, violations.size() );
		assertSame( customer.address, violations.iterator().next().getRootBean() );
		assertEquals( "city", violations.iterator().next().getPropertyPath().toString() );
		//end::nestedPropertyWithoutValid[]
	}

	//tag::nestedPropertyBeans[]
	public static class Address {
		@NotNull
		public String city;

		@NotNull
		public String street;
	}

	public static class Customer {
		@Valid
		public Address address = new Address();
	}
	//end::nestedPropertyBeans[]

	//tag::nestedPropertyWithoutValidBean[]
	public static class CustomerWithoutValid {
		public Address address = new Address();
	}
	//end::nestedPropertyWithoutValidBean[]
}
