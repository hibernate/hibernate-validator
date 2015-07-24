/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import org.hibernate.validator.internal.constraintvalidators.bv.DigitsValidatorForCharSequence;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.validation.constraints.Digits;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Alaa Nassef
 */
public class DigitsValidatorForCharSequenceTest {

	private static DigitsValidatorForCharSequence constraint;

	@BeforeClass
	public static void init() {

		AnnotationDescriptor<Digits> descriptor = new AnnotationDescriptor<Digits>( Digits.class );
		descriptor.setValue( "integer", 5 );
		descriptor.setValue( "fraction", 2 );
		descriptor.setValue( "message", "{validator.digits}" );
		Digits p = AnnotationFactory.create( descriptor );

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

		AnnotationDescriptor<Digits> descriptor = new AnnotationDescriptor<Digits>( Digits.class );
		descriptor.setValue( "integer", -1 );
		descriptor.setValue( "fraction", 1 );
		descriptor.setValue( "message", "{validator.digits}" );
		Digits p = AnnotationFactory.create( descriptor );

		DigitsValidatorForCharSequence constraint = new DigitsValidatorForCharSequence();
		constraint.initialize( p );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeFractionLength() {

		AnnotationDescriptor<Digits> descriptor = new AnnotationDescriptor<Digits>( Digits.class );
		descriptor.setValue( "integer", 1 );
		descriptor.setValue( "fraction", -1 );
		descriptor.setValue( "message", "{validator.digits}" );
		Digits p = AnnotationFactory.create( descriptor );

		DigitsValidatorForCharSequence constraint = new DigitsValidatorForCharSequence();
		constraint.initialize( p );
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void testIsValidCharSequence() {
		assertTrue( constraint.isValid( new MyCustomStringImpl( "500.2" ), null ) );
	}
}
