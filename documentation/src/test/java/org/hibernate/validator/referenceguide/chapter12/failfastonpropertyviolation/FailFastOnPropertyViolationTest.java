/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter12.failfastonpropertyviolation;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.HibernateValidator;

import org.junit.Test;

public class FailFastOnPropertyViolationTest {

	@Test
	public void failFastOnPropertyViolation() {
		//tag::include[]
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.failFastOnPropertyViolation( true )
				.buildValidatorFactory()
				.getValidator();

		Book book = new Book( "978-1-56619-909-4", "Book", null /* author */, null /* publisher */ );

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book );

		assertEquals( 2, constraintViolations.size() );

		for ( ConstraintViolation<Book> constraintViolation : constraintViolations ) {
			assertEquals(
					NotNull.class,
					constraintViolation.getConstraintDescriptor()
							.getAnnotation()
							.annotationType()
			);
		}
		//end::include[]
	}

	@Test
	public void failFastOnPropertyViolationProperty() {
		//tag::property[]
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addProperty( "hibernate.validator.fail_fast_on_property_violation", "true" )
				.buildValidatorFactory()
				.getValidator();
		//end::property[]

		Book book = new Book( "978-1-56619-909-4", "Book", null /* author */, null /* publisher */ );

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( book );

		assertEquals( 2, constraintViolations.size() );
	}
}
