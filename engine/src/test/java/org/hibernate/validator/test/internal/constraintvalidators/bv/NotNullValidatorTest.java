/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;

import org.testng.annotations.Test;

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
