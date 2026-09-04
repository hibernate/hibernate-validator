/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.constraints.Trimmed;
import org.hibernate.validator.internal.constraintvalidators.hv.TrimmedValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the {@link Trimmed} constraint.
 */
public class TrimmedValidatorTest {

	private TrimmedValidator constraint;

	@BeforeMethod
	public void setUp() {
		ConstraintAnnotationDescriptor.Builder<Trimmed> descriptorBuilder =
				new ConstraintAnnotationDescriptor.Builder<>( Trimmed.class );
		Trimmed annotation = descriptorBuilder.build().getAnnotation();
		constraint = new TrimmedValidator();
		constraint.initialize( annotation );
	}

	@Test
	public void testNullAndEmptyAreValid() {
		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( "", null ) );
	}

	@Test
	public void testValidValues() {
		assertTrue( constraint.isValid( "foobar", null ) );
		assertTrue( constraint.isValid( "a", null ) );
		assertTrue( constraint.isValid( "foo bar", null ) );
		// internal whitespace is allowed, only the ends matter
		assertTrue( constraint.isValid( "foo\tbar\nbaz", null ) );
	}

	@Test
	public void testLeadingWhitespaceIsInvalid() {
		assertFalse( constraint.isValid( " foobar", null ) );
		assertFalse( constraint.isValid( "\tfoobar", null ) );
		assertFalse( constraint.isValid( "\nfoobar", null ) );
	}

	@Test
	public void testTrailingWhitespaceIsInvalid() {
		assertFalse( constraint.isValid( "foobar ", null ) );
		assertFalse( constraint.isValid( "foobar\t", null ) );
		assertFalse( constraint.isValid( "foobar\n", null ) );
	}

	@Test
	public void testLeadingAndTrailingWhitespaceIsInvalid() {
		assertFalse( constraint.isValid( " foobar ", null ) );
		// a lone whitespace character is both leading and trailing
		assertFalse( constraint.isValid( " ", null ) );
	}

	@Test
	public void testUnicodeWhitespaceIsInvalid() {
		// em space (U+2003) is Unicode whitespace, matching String#strip() semantics
		assertFalse( constraint.isValid( " foobar", null ) );
		assertFalse( constraint.isValid( "foobar ", null ) );
	}

	@Test
	public void testNonBreakingSpaceIsValid() {
		// a non-breaking space (U+00A0) is not whitespace per Character#isWhitespace, as with String#strip()
		assertTrue( constraint.isValid( " foobar ", null ) );
	}

	@Test
	public void testCharSequence() {
		assertTrue( constraint.isValid( new MyCustomStringImpl( "foobar" ), null ) );
		assertFalse( constraint.isValid( new MyCustomStringImpl( " foobar " ), null ) );
	}
}
