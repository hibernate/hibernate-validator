/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.constraints.StartsWith;
import org.hibernate.validator.internal.constraintvalidators.hv.StartsWithValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the {@link StartsWith} constraint.
 *
 * @author Andrea Boriero
 * @author Koen Aers
 */
public class StartsWithValidatorTest {

	private ConstraintAnnotationDescriptor.Builder<StartsWith> descriptorBuilder;

	@BeforeMethod
	public void setUp() throws Exception {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( StartsWith.class );
	}

	@Test
	public void testNullIsValid() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo" } );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void testValidPrefix() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo" } );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testInvalidPrefix() {
		descriptorBuilder.setAttribute( "value", new String[] { "bar" } );
		StartsWithValidator validator = createValidator();
		assertFalse( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testExactMatch() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo" } );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "foo", null ) );
	}

	@Test
	public void testPrefixLongerThanValue() {
		descriptorBuilder.setAttribute( "value", new String[] { "foobar" } );
		StartsWithValidator validator = createValidator();
		assertFalse( validator.isValid( "foo", null ) );
	}

	@Test
	public void testEmptyPrefix() {
		descriptorBuilder.setAttribute( "value", new String[] { "" } );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "anything", null ) );
	}

	@Test
	public void testEmptyStringWithNonEmptyPrefix() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo" } );
		StartsWithValidator validator = createValidator();
		assertFalse( validator.isValid( "", null ) );
	}

	@Test
	public void testIgnoreCaseTrue() {
		descriptorBuilder.setAttribute( "value", new String[] { "FOO" } );
		descriptorBuilder.setAttribute( "ignoreCase", true );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testIgnoreCaseFalse() {
		descriptorBuilder.setAttribute( "value", new String[] { "FOO" } );
		descriptorBuilder.setAttribute( "ignoreCase", false );
		StartsWithValidator validator = createValidator();
		assertFalse( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testIgnoreCaseTrueWithMixedCase() {
		descriptorBuilder.setAttribute( "value", new String[] { "FoO" } );
		descriptorBuilder.setAttribute( "ignoreCase", true );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "fOoBaR", null ) );
	}

	@Test
	public void testIgnoreCaseTrueValidatesLowerCasePrefix() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo" } );
		descriptorBuilder.setAttribute( "ignoreCase", true );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "FOObar", null ) );
	}

	@Test
	public void testCharSequenceType() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo" } );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( new MyCustomStringImpl( "foobar" ), null ) );
	}

	@Test
	public void testStringBuilderType() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo" } );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( new StringBuilder( "foobar" ), null ) );
	}

	@Test
	public void testMultiplePrefixesMatchesFirst() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo", "bar" } );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "foobaz", null ) );
	}

	@Test
	public void testMultiplePrefixesMatchesSecond() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo", "bar" } );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "barbaz", null ) );
	}

	@Test
	public void testMultiplePrefixesMatchesNone() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo", "bar" } );
		StartsWithValidator validator = createValidator();
		assertFalse( validator.isValid( "bazqux", null ) );
	}

	@Test
	public void testMultiplePrefixesIgnoreCase() {
		descriptorBuilder.setAttribute( "value", new String[] { "FOO", "BAR" } );
		descriptorBuilder.setAttribute( "ignoreCase", true );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "barbaz", null ) );
	}

	private StartsWithValidator createValidator() {
		StartsWith annotation = descriptorBuilder.build().getAnnotation();
		StartsWithValidator validator = new StartsWithValidator();
		validator.initialize( annotation );
		return validator;
	}
}
