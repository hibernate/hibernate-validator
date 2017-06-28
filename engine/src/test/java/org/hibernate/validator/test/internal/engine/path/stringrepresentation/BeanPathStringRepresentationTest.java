/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.path.stringrepresentation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class BeanPathStringRepresentationTest extends AbstractPathStringRepresentationTest {
	@Test
	public void testBeanPath() throws Exception {
		Address address = new Address( "str", new City( "Lyon" ), "invalid zip" );
		Set<ConstraintViolation<Address>> constraintViolations = validator.validate( address );

		assertCorrectPropertyPaths( constraintViolations, "" );
	}

	@Test
	public void testBeanPropertyPath() throws Exception {
		Address address = new Address( null, new City( "" ) );
		Set<ConstraintViolation<Address>> constraintViolations = validator.validate( address );

		assertCorrectPropertyPaths( constraintViolations, "street", "city.name" );
	}

}
