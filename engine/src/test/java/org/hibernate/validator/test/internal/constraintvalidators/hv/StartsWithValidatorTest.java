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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the {@link StartsWithValidator} constraint validator.
 *
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
	public void testMatchingPrefixIsValid() {
		descriptorBuilder.setAttribute( "value", new String[] { "fu" } );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "fubar", null ) );
	}

	@Test
	public void testNonMatchingPrefixIsInvalid() {
		descriptorBuilder.setAttribute( "value", new String[] { "fu" } );
		StartsWithValidator validator = createValidator();
		assertFalse( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testEmptyPrefixAlwaysMatches() {
		descriptorBuilder.setAttribute( "value", new String[] { "" } );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "foobar", null ) );
		assertTrue( validator.isValid( "", null ) );
	}

	@Test
	public void testCaseSensitiveByDefault() {
		descriptorBuilder.setAttribute( "value", new String[] { "FU" } );
		StartsWithValidator validator = createValidator();
		assertFalse( validator.isValid( "fubar", null ) );
	}

	@Test
	public void testIgnoreCaseMatches() {
		descriptorBuilder.setAttribute( "value", new String[] { "FU" } );
		descriptorBuilder.setAttribute( "ignoreCase", true );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "fubar", null ) );
	}

	@Test
	public void testMultipleValuesOrSemantics() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo", "fu" } );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "foobar", null ) );
		assertTrue( validator.isValid( "fubar", null ) );
		assertFalse( validator.isValid( "barfoo", null ) );
	}

	@Test
	public void testMultipleValuesNoneMatches() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo", "bar" } );
		StartsWithValidator validator = createValidator();
		assertFalse( validator.isValid( "baz", null ) );
	}

	private StartsWithValidator createValidator() {
		StartsWith annotation = descriptorBuilder.build().getAnnotation();
		StartsWithValidator validator = new StartsWithValidator();
		validator.initialize( annotation );
		return validator;
	}
}
