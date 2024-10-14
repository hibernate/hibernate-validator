/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.internal.constraintvalidators.bv.AssertTrueValidator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
