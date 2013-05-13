/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.internal.constraintvalidators;

import javax.validation.constraints.Pattern;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.internal.constraintvalidators.PatternValidator;
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
