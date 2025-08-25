/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.hibernate.validator.testutils.ConstraintValidatorInitializationHelper.initialize;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.internal.constraintvalidators.bv.PatternValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class PatternValidatorTest {

	@Test
	public void testIsValid() {
		ConstraintAnnotationDescriptor.Builder<Pattern> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Pattern.class );
		descriptorBuilder.setAttribute( "regexp", "foobar" );
		descriptorBuilder.setMessage( "pattern does not match" );
		ConstraintAnnotationDescriptor<Pattern> descriptor = descriptorBuilder.build();

		PatternValidator constraint = new PatternValidator();
		initialize( constraint, descriptor );

		assertTrue( constraint.isValid( null, null ) );
		assertFalse( constraint.isValid( "", null ) );
		assertFalse( constraint.isValid( "bla bla", null ) );
		assertFalse( constraint.isValid( "This test is not foobar", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void testIsValidForCharSequence() {
		ConstraintAnnotationDescriptor.Builder<Pattern> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Pattern.class );
		descriptorBuilder.setAttribute( "regexp", "char sequence" );
		ConstraintAnnotationDescriptor<Pattern> descriptor = descriptorBuilder.build();

		PatternValidator constraint = new PatternValidator();
		initialize( constraint, descriptor );

		assertTrue( constraint.isValid( new MyCustomStringImpl( "char sequence" ), null ) );
	}

	@Test
	public void testIsValidForEmptyStringRegexp() {
		ConstraintAnnotationDescriptor.Builder<Pattern> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Pattern.class );
		descriptorBuilder.setAttribute( "regexp", "|^.*foo$" );
		descriptorBuilder.setMessage( "pattern does not match" );
		ConstraintAnnotationDescriptor<Pattern> descriptor = descriptorBuilder.build();

		PatternValidator constraint = new PatternValidator();
		initialize( constraint, descriptor );

		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( "", null ) );
		assertFalse( constraint.isValid( "bla bla", null ) );
		assertTrue( constraint.isValid( "foo", null ) );
		assertTrue( constraint.isValid( "a b c foo", null ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidRegularExpression() {
		ConstraintAnnotationDescriptor.Builder<Pattern> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Pattern.class );
		descriptorBuilder.setAttribute( "regexp", "(unbalanced parentheses" );
		descriptorBuilder.setMessage( "pattern does not match" );
		ConstraintAnnotationDescriptor<Pattern> descriptor = descriptorBuilder.build();

		PatternValidator constraint = new PatternValidator();
		initialize( constraint, descriptor );
	}
}
