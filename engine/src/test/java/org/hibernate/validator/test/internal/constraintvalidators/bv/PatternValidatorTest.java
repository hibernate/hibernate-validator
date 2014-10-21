/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import javax.validation.constraints.Pattern;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.internal.constraintvalidators.bv.PatternValidator;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Hardy Ferentschik
 */
public class PatternValidatorTest {

	@Test
	public void testIsValid() {
		AnnotationDescriptor<Pattern> descriptor = new AnnotationDescriptor<Pattern>( Pattern.class );
		descriptor.setValue( "regexp", "foobar" );
		descriptor.setValue( "message", "pattern does not match" );
		Pattern p = AnnotationFactory.create( descriptor );

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
		AnnotationDescriptor<Pattern> descriptor = new AnnotationDescriptor<Pattern>( Pattern.class );
		descriptor.setValue( "regexp", "char sequence" );
		Pattern p = AnnotationFactory.create( descriptor );

		PatternValidator constraint = new PatternValidator();
		constraint.initialize( p );

		assertTrue( constraint.isValid( new MyCustomStringImpl( "char sequence" ), null ) );
	}

	@Test
	public void testIsValidForEmptyStringRegexp() {
		AnnotationDescriptor<Pattern> descriptor = new AnnotationDescriptor<Pattern>( Pattern.class );
		descriptor.setValue( "regexp", "|^.*foo$" );
		descriptor.setValue( "message", "pattern does not match" );
		Pattern p = AnnotationFactory.create( descriptor );

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
		AnnotationDescriptor<Pattern> descriptor = new AnnotationDescriptor<Pattern>( Pattern.class );
		descriptor.setValue( "regexp", "(unbalanced parentheses" );
		descriptor.setValue( "message", "pattern does not match" );
		Pattern p = AnnotationFactory.create( descriptor );

		PatternValidator constraint = new PatternValidator();
		constraint.initialize( p );
	}
}
