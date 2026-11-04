/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hibernate.validator.internal.constraintvalidators.bv.NullValidator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * @author Alaa Nassef
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NullValidatorTest {

	private static NullValidator constraint;

	@BeforeAll
	public static void init() {
		constraint = new NullValidator();
	}

	@Test
	public void testIsValid() {
		assertTrue( constraint.isValid( null, null ) );
		assertFalse( constraint.isValid( new Object(), null ) );
	}
}
