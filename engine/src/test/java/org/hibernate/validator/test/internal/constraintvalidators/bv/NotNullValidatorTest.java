/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Hardy Ferentschik
 */
public class NotNullValidatorTest {

	@Test
	public void testIsValid() {
		NotNullValidator constraint = new NotNullValidator();

		assertFalse( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( new Object(), null ) );
	}
}
