/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.internal.constraintvalidators.hv.LengthValidator;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

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
