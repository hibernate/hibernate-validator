/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.internal.constraintvalidators.bv.NullValidator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Alaa Nassef
 */
public class NullValidatorTest {

	private static NullValidator constraint;

	@BeforeClass
	public static void init() {
		constraint = new NullValidator();
	}

	@Test
	public void testIsValid() {
		assertTrue( constraint.isValid( null, null ) );
		assertFalse( constraint.isValid( new Object(), null ) );
	}
}
