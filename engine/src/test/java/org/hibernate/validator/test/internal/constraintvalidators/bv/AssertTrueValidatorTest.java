/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hibernate.validator.internal.constraintvalidators.bv.AssertTrueValidator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * @author Alaa Nassef
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AssertTrueValidatorTest {

	private static AssertTrueValidator constraint;

	@BeforeAll
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
