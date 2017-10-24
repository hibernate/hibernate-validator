/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.internal.constraintvalidators.hv.LengthValidator;
import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

/**
 * Tests the {@code LengthConstraint}.
 *
 * @author Hardy Ferentschik
 */
public class LengthValidatorTest {

	@Test
	public void testIsValid() {
		AnnotationDescriptor.Builder<Length> descriptorBuilder = new AnnotationDescriptor.Builder<>( Length.class );
		descriptorBuilder.setAttribute( "min", 1 );
		descriptorBuilder.setAttribute( "max", 3 );
		descriptorBuilder.setAttribute( "message", "{validator.length}" );
		Length l = descriptorBuilder.build().getAnnotation();
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
		AnnotationDescriptor.Builder<Length> descriptorBuilder = new AnnotationDescriptor.Builder<>( Length.class );
		descriptorBuilder.setAttribute( "min", 1 );
		descriptorBuilder.setAttribute( "max", 3 );
		Length l = descriptorBuilder.build().getAnnotation();
		LengthValidator constraint = new LengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( new MyCustomStringImpl( "foo" ), null ) );
		assertFalse( constraint.isValid( new MyCustomStringImpl( "foobar" ), null ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeMinValue() {
		AnnotationDescriptor.Builder<Length> descriptorBuilder = new AnnotationDescriptor.Builder<>( Length.class );
		descriptorBuilder.setAttribute( "min", -1 );
		descriptorBuilder.setAttribute( "max", 1 );
		descriptorBuilder.setAttribute( "message", "{validator.length}" );
		Length p = descriptorBuilder.build().getAnnotation();

		LengthValidator constraint = new LengthValidator();
		constraint.initialize( p );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeMaxValue() {
		AnnotationDescriptor.Builder<Length> descriptorBuilder = new AnnotationDescriptor.Builder<>( Length.class );
		descriptorBuilder.setAttribute( "min", 1 );
		descriptorBuilder.setAttribute( "max", -1 );
		descriptorBuilder.setAttribute( "message", "{validator.length}" );
		Length p = descriptorBuilder.build().getAnnotation();

		LengthValidator constraint = new LengthValidator();
		constraint.initialize( p );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeLength() {
		AnnotationDescriptor.Builder<Length> descriptorBuilder = new AnnotationDescriptor.Builder<>( Length.class );
		descriptorBuilder.setAttribute( "min", 5 );
		descriptorBuilder.setAttribute( "max", 4 );
		descriptorBuilder.setAttribute( "message", "{validator.length}" );
		Length p = descriptorBuilder.build().getAnnotation();

		LengthValidator constraint = new LengthValidator();
		constraint.initialize( p );
	}
}
