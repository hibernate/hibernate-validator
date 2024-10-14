/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.internal.constraintvalidators.bv.AssertFalseValidator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Alaa Nassef
 */
public class AssertFalseValidatorTest {

	private static AssertFalseValidator constraint;

	@BeforeClass
	public static void init() {
		constraint = new AssertFalseValidator();
	}

	@Test
	public void testIsValid() {
		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( false, null ) );
		assertTrue( constraint.isValid( Boolean.FALSE, null ) );
		assertFalse( constraint.isValid( true, null ) );
		assertFalse( constraint.isValid( Boolean.TRUE, null ) );
	}
}
