/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.path.stringrepresentation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPathStringRepresentations;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class BeanPathStringRepresentationTest extends AbstractPathStringRepresentationTest {

	@Test
	public void testBeanPath() throws Exception {
		Address address = new Address( "str", new City( "Lyon" ), "invalid zip" );
		Set<ConstraintViolation<Address>> constraintViolations = validator.validate( address );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "" );
	}

	@Test
	public void testBeanPropertyPath() throws Exception {
		Address address = new Address( null, new City( "" ) );
		Set<ConstraintViolation<Address>> constraintViolations = validator.validate( address );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "street", "city.name" );
	}
}
