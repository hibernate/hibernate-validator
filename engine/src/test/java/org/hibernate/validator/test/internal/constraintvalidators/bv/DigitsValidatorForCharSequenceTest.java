/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import javax.validation.constraints.Digits;

import org.hibernate.validator.internal.constraintvalidators.bv.DigitsValidatorForCharSequence;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Alaa Nassef
 */
public class DigitsValidatorForCharSequenceTest {

	private static DigitsValidatorForCharSequence constraint;

	@BeforeClass
	public static void init() {

		AnnotationDescriptor.Builder<Digits> descriptorBuilder = new AnnotationDescriptor.Builder<>( Digits.class );
		descriptorBuilder.setValue( "integer", 5 );
		descriptorBuilder.setValue( "fraction", 2 );
		descriptorBuilder.setValue( "message", "{validator.digits}" );
		Digits p = descriptorBuilder.build().annotation();

		constraint = new DigitsValidatorForCharSequence();
		constraint.initialize( p );
	}

	@Test
	public void testIsValid() {

		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( "0", null ) );
		assertTrue( constraint.isValid( "500.2", null ) );
		assertTrue( constraint.isValid( "-12456.22", null ) );
		assertTrue( constraint.isValid( "-000000000.22", null ) );
		//should throw number format exception
		assertFalse( constraint.isValid( "", null ) );
		assertFalse( constraint.isValid( "256874.0", null ) );
		assertFalse( constraint.isValid( "12.0001", null ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeIntegerLength() {

		AnnotationDescriptor.Builder<Digits> descriptorBuilder = new AnnotationDescriptor.Builder<>( Digits.class );
		descriptorBuilder.setValue( "integer", -1 );
		descriptorBuilder.setValue( "fraction", 1 );
		descriptorBuilder.setValue( "message", "{validator.digits}" );
		Digits p = descriptorBuilder.build().annotation();

		DigitsValidatorForCharSequence constraint = new DigitsValidatorForCharSequence();
		constraint.initialize( p );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeFractionLength() {

		AnnotationDescriptor.Builder<Digits> descriptorBuilder = new AnnotationDescriptor.Builder<>( Digits.class );
		descriptorBuilder.setValue( "integer", 1 );
		descriptorBuilder.setValue( "fraction", -1 );
		descriptorBuilder.setValue( "message", "{validator.digits}" );
		Digits p = descriptorBuilder.build().annotation();

		DigitsValidatorForCharSequence constraint = new DigitsValidatorForCharSequence();
		constraint.initialize( p );
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void testIsValidCharSequence() {
		assertTrue( constraint.isValid( new MyCustomStringImpl( "500.2" ), null ) );
	}
}
