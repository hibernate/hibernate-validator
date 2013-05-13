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

import org.testng.annotations.Test;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.internal.constraintvalidators.LengthValidator;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests the <code>LengthConstraint</code>.
 *
 * @author Hardy Ferentschik
 */
public class LengthValidatorTest {

	@Test
	public void testIsValid() {
		AnnotationDescriptor<Length> descriptor = new AnnotationDescriptor<Length>( Length.class );
		descriptor.setValue( "min", 1 );
		descriptor.setValue( "max", 3 );
		descriptor.setValue( "message", "{validator.length}" );
		Length l = AnnotationFactory.create( descriptor );
		LengthValidator constraint = new LengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( null, null ) );
		assertFalse( constraint.isValid( "", null ) );
		assertTrue( constraint.isValid( "f", null ) );
		assertTrue( constraint.isValid( "fo", null ) );
		assertTrue( constraint.isValid( "foo", null ) );
		assertFalse( constraint.isValid( "foobar", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void testIsValidCharSequence() {
		AnnotationDescriptor<Length> descriptor = new AnnotationDescriptor<Length>( Length.class );
		descriptor.setValue( "min", 1 );
		descriptor.setValue( "max", 3 );
		Length l = AnnotationFactory.create( descriptor );
		LengthValidator constraint = new LengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( new MyCustomStringImpl( "foo" ), null ) );
		assertFalse( constraint.isValid( new MyCustomStringImpl( "foobar" ), null ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeMinValue() {
		AnnotationDescriptor<Length> descriptor = new AnnotationDescriptor<Length>( Length.class );
		descriptor.setValue( "min", -1 );
		descriptor.setValue( "max", 1 );
		descriptor.setValue( "message", "{validator.length}" );
		Length p = AnnotationFactory.create( descriptor );

		LengthValidator constraint = new LengthValidator();
		constraint.initialize( p );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeMaxValue() {
		AnnotationDescriptor<Length> descriptor = new AnnotationDescriptor<Length>( Length.class );
		descriptor.setValue( "min", 1 );
		descriptor.setValue( "max", -1 );
		descriptor.setValue( "message", "{validator.length}" );
		Length p = AnnotationFactory.create( descriptor );

		LengthValidator constraint = new LengthValidator();
		constraint.initialize( p );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeLength() {
		AnnotationDescriptor<Length> descriptor = new AnnotationDescriptor<Length>( Length.class );
		descriptor.setValue( "min", 5 );
		descriptor.setValue( "max", 4 );
		descriptor.setValue( "message", "{validator.length}" );
		Length p = AnnotationFactory.create( descriptor );

		LengthValidator constraint = new LengthValidator();
		constraint.initialize( p );
	}
}
