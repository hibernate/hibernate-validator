/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.constraints.EndsWith;
import org.hibernate.validator.internal.constraintvalidators.hv.EndsWithValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the {@link EndsWith} constraint.
 *
 * @author Andrea Boriero
 */
public class EndsWithValidatorTest {

	private ConstraintAnnotationDescriptor.Builder<EndsWith> descriptorBuilder;

	@BeforeMethod
	public void setUp() throws Exception {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( EndsWith.class );
	}

	@Test
	public void testNullIsValid() {
		descriptorBuilder.setAttribute( "value", new String[] { "bar" } );
		EndsWithValidator validator = createValidator();
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void testValidSuffix() {
		descriptorBuilder.setAttribute( "value", new String[] { "bar" } );
		EndsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testInvalidSuffix() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo" } );
		EndsWithValidator validator = createValidator();
		assertFalse( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testExactMatch() {
		descriptorBuilder.setAttribute( "value", new String[] { "bar" } );
		EndsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "bar", null ) );
	}

	@Test
	public void testSuffixLongerThanValue() {
		descriptorBuilder.setAttribute( "value", new String[] { "foobar" } );
		EndsWithValidator validator = createValidator();
		assertFalse( validator.isValid( "bar", null ) );
	}

	@Test
	public void testEmptySuffix() {
		descriptorBuilder.setAttribute( "value", new String[] { "" } );
		EndsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "anything", null ) );
	}

	@Test
	public void testEmptyStringWithNonEmptySuffix() {
		descriptorBuilder.setAttribute( "value", new String[] { "bar" } );
		EndsWithValidator validator = createValidator();
		assertFalse( validator.isValid( "", null ) );
	}

	@Test
	public void testIgnoreCaseTrue() {
		descriptorBuilder.setAttribute( "value", new String[] { "BAR" } );
		descriptorBuilder.setAttribute( "ignoreCase", true );
		EndsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testIgnoreCaseFalse() {
		descriptorBuilder.setAttribute( "value", new String[] { "BAR" } );
		descriptorBuilder.setAttribute( "ignoreCase", false );
		EndsWithValidator validator = createValidator();
		assertFalse( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testIgnoreCaseTrueWithMixedCase() {
		descriptorBuilder.setAttribute( "value", new String[] { "BaR" } );
		descriptorBuilder.setAttribute( "ignoreCase", true );
		EndsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "fOoBaR", null ) );
	}

	@Test
	public void testIgnoreCaseTrueValidatesLowerCaseSuffix() {
		descriptorBuilder.setAttribute( "value", new String[] { "bar" } );
		descriptorBuilder.setAttribute( "ignoreCase", true );
		EndsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "fooBAR", null ) );
	}

	@Test
	public void testCharSequenceType() {
		descriptorBuilder.setAttribute( "value", new String[] { "bar" } );
		EndsWithValidator validator = createValidator();
		assertTrue( validator.isValid( new MyCustomStringImpl( "foobar" ), null ) );
	}

	@Test
	public void testStringBuilderType() {
		descriptorBuilder.setAttribute( "value", new String[] { "bar" } );
		EndsWithValidator validator = createValidator();
		assertTrue( validator.isValid( new StringBuilder( "foobar" ), null ) );
	}

	@Test
	public void testMultipleSuffixesMatchesFirst() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo", "bar" } );
		EndsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "bazfoo", null ) );
	}

	@Test
	public void testMultipleSuffixesMatchesSecond() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo", "bar" } );
		EndsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "bazbar", null ) );
	}

	@Test
	public void testMultipleSuffixesMatchesNone() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo", "bar" } );
		EndsWithValidator validator = createValidator();
		assertFalse( validator.isValid( "bazqux", null ) );
	}

	@Test
	public void testMultipleSuffixesIgnoreCase() {
		descriptorBuilder.setAttribute( "value", new String[] { "FOO", "BAR" } );
		descriptorBuilder.setAttribute( "ignoreCase", true );
		EndsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "bazbar", null ) );
	}

	private EndsWithValidator createValidator() {
		EndsWith annotation = descriptorBuilder.build().getAnnotation();
		EndsWithValidator validator = new EndsWithValidator();
		validator.initialize( annotation );
		return validator;
	}
}
