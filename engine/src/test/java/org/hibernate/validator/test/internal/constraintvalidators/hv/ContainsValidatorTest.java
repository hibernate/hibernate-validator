/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.constraints.Contains;
import org.hibernate.validator.internal.constraintvalidators.hv.ContainsValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the {@link Contains} constraint.
 *
 * @author Sean Okafor
 */
public class ContainsValidatorTest {

	private ConstraintAnnotationDescriptor.Builder<Contains> descriptorBuilder;

	@BeforeMethod
	public void setUp() throws Exception {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Contains.class );
	}

	@Test
	public void testNullIsValid() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo" } );
		ContainsValidator validator = createValidator();
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void testSingleValuePresent() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo" } );
		ContainsValidator validator = createValidator();
		assertTrue( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testSingleValueAbsent() {
		descriptorBuilder.setAttribute( "value", new String[] { "baz" } );
		ContainsValidator validator = createValidator();
		assertFalse( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testMultipleValuesAllPresent() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo", "bar" } );
		ContainsValidator validator = createValidator();
		assertTrue( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testMultipleValuesOneMissing() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo", "baz" } );
		ContainsValidator validator = createValidator();
		assertFalse( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testMinRequiredWithEnoughMatches() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo", "bar", "baz" } );
		descriptorBuilder.setAttribute( "minRequired", 2 );
		ContainsValidator validator = createValidator();
		assertTrue( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testMinRequiredWithNotEnoughMatches() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo", "bar", "baz" } );
		descriptorBuilder.setAttribute( "minRequired", 2 );
		ContainsValidator validator = createValidator();
		assertFalse( validator.isValid( "foo-qux", null ) );
	}

	@Test
	public void testMinRequiredOneActsAsOr() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo", "bar", "baz" } );
		descriptorBuilder.setAttribute( "minRequired", 1 );
		ContainsValidator validator = createValidator();
		assertTrue( validator.isValid( "baz-only", null ) );
	}

	@Test
	public void testMinRequiredOneNonePresent() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo", "bar", "baz" } );
		descriptorBuilder.setAttribute( "minRequired", 1 );
		ContainsValidator validator = createValidator();
		assertFalse( validator.isValid( "qux-only", null ) );
	}

	@Test
	public void testIgnoreCaseTrue() {
		descriptorBuilder.setAttribute( "value", new String[] { "FOO" } );
		descriptorBuilder.setAttribute( "ignoreCase", true );
		ContainsValidator validator = createValidator();
		assertTrue( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testIgnoreCaseFalse() {
		descriptorBuilder.setAttribute( "value", new String[] { "FOO" } );
		descriptorBuilder.setAttribute( "ignoreCase", false );
		ContainsValidator validator = createValidator();
		assertFalse( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testIgnoreCaseWithMinRequired() {
		descriptorBuilder.setAttribute( "value", new String[] { "FOO", "BAR", "BAZ" } );
		descriptorBuilder.setAttribute( "ignoreCase", true );
		descriptorBuilder.setAttribute( "minRequired", 2 );
		ContainsValidator validator = createValidator();
		assertTrue( validator.isValid( "foo-bar", null ) );
	}

	@Test
	public void testEmptyValuesArray() {
		descriptorBuilder.setAttribute( "value", new String[] { } );
		ContainsValidator validator = createValidator();
		assertTrue( validator.isValid( "anything", null ) );
	}

	@Test
	public void testEmptyStringInput() {
		descriptorBuilder.setAttribute( "value", new String[] { "" } );
		ContainsValidator validator = createValidator();
		assertTrue( validator.isValid( "anything", null ) );
	}

	@Test
	public void testEmptyStringInputWithNonEmptyValue() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo" } );
		ContainsValidator validator = createValidator();
		assertFalse( validator.isValid( "", null ) );
	}

	@Test
	public void testCharSequenceType() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo" } );
		ContainsValidator validator = createValidator();
		assertTrue( validator.isValid( new MyCustomStringImpl( "foobar" ), null ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testMinRequiredExceedsValueCount() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo", "bar" } );
		descriptorBuilder.setAttribute( "minRequired", 5 );
		createValidator();
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeMinRequired() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo" } );
		descriptorBuilder.setAttribute( "minRequired", -2 );
		createValidator();
	}

	private ContainsValidator createValidator() {
		Contains annotation = descriptorBuilder.build().getAnnotation();
		ContainsValidator validator = new ContainsValidator();
		validator.initialize( annotation );
		return validator;
	}
}
