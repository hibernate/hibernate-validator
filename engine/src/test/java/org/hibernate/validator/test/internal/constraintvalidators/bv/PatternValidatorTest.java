/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.internal.constraintvalidators.bv.PatternValidator;
import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class PatternValidatorTest {

	@Test
	public void testIsValid() {
		AnnotationDescriptor.Builder<Pattern> descriptorBuilder = new AnnotationDescriptor.Builder<>( Pattern.class );
		descriptorBuilder.setAttribute( "regexp", "foobar" );
		descriptorBuilder.setAttribute( "message", "pattern does not match" );
		Pattern p = descriptorBuilder.build().annotation();

		PatternValidator constraint = new PatternValidator();
		constraint.initialize( p );

		assertTrue( constraint.isValid( null, null ) );
		assertFalse( constraint.isValid( "", null ) );
		assertFalse( constraint.isValid( "bla bla", null ) );
		assertFalse( constraint.isValid( "This test is not foobar", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void testIsValidForCharSequence() {
		AnnotationDescriptor.Builder<Pattern> descriptorBuilder = new AnnotationDescriptor.Builder<>( Pattern.class );
		descriptorBuilder.setAttribute( "regexp", "char sequence" );
		Pattern p = descriptorBuilder.build().annotation();

		PatternValidator constraint = new PatternValidator();
		constraint.initialize( p );

		assertTrue( constraint.isValid( new MyCustomStringImpl( "char sequence" ), null ) );
	}

	@Test
	public void testIsValidForEmptyStringRegexp() {
		AnnotationDescriptor.Builder<Pattern> descriptorBuilder = new AnnotationDescriptor.Builder<>( Pattern.class );
		descriptorBuilder.setAttribute( "regexp", "|^.*foo$" );
		descriptorBuilder.setAttribute( "message", "pattern does not match" );
		Pattern p = descriptorBuilder.build().annotation();

		PatternValidator constraint = new PatternValidator();
		constraint.initialize( p );

		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( "", null ) );
		assertFalse( constraint.isValid( "bla bla", null ) );
		assertTrue( constraint.isValid( "foo", null ) );
		assertTrue( constraint.isValid( "a b c foo", null ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidRegularExpression() {
		AnnotationDescriptor.Builder<Pattern> descriptorBuilder = new AnnotationDescriptor.Builder<>( Pattern.class );
		descriptorBuilder.setAttribute( "regexp", "(unbalanced parentheses" );
		descriptorBuilder.setAttribute( "message", "pattern does not match" );
		Pattern p = descriptorBuilder.build().annotation();

		PatternValidator constraint = new PatternValidator();
		constraint.initialize( p );
	}
}
