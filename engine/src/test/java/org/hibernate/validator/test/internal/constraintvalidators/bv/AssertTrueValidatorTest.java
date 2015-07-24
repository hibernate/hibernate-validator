/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import org.hibernate.validator.internal.constraintvalidators.bv.AssertTrueValidator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Alaa Nassef
 */
public class AssertTrueValidatorTest {

	private static AssertTrueValidator constraint;

	@BeforeClass
	public static void init() {
		constraint = new AssertTrueValidator();
	}

	@Test
	public void testIsValid() {
		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( true, null ) );
		assertTrue( constraint.isValid( Boolean.TRUE, null ) );
		assertFalse( constraint.isValid( false, null ) );
		assertFalse( constraint.isValid( Boolean.FALSE, null ) );
	}
}

