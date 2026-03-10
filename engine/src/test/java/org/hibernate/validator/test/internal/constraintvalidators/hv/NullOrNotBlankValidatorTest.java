/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.internal.constraintvalidators.hv.NullOrNotBlankValidator;

import org.testng.annotations.Test;

/**
 * Tests the {@link NullOrNotBlankValidator} constraint validator.
 *
 * @author Koen Aers
 */
public class NullOrNotBlankValidatorTest {

	private final NullOrNotBlankValidator validator = new NullOrNotBlankValidator();

	@Test
	public void nullIsValid() {
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void notBlankIsValid() {
		assertTrue( validator.isValid( "a", null ) );
		assertTrue( validator.isValid( "foobar", null ) );
		assertTrue( validator.isValid( " a ", null ) );
	}

	@Test
	public void emptyIsInvalid() {
		assertFalse( validator.isValid( "", null ) );
	}

	@Test
	public void blankIsInvalid() {
		assertFalse( validator.isValid( " ", null ) );
		assertFalse( validator.isValid( "\t", null ) );
		assertFalse( validator.isValid( "\n", null ) );
		assertFalse( validator.isValid( "   \t\n  ", null ) );
	}

}
