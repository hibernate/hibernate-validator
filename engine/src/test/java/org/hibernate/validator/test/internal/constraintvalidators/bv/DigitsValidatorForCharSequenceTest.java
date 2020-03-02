/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import jakarta.validation.constraints.Digits;

import org.hibernate.validator.internal.constraintvalidators.bv.DigitsValidatorForCharSequence;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Alaa Nassef
 */
public class DigitsValidatorForCharSequenceTest {

	private static DigitsValidatorForCharSequence constraint;
	private ConstraintAnnotationDescriptor.Builder<Digits> descriptorBuilder;

	@BeforeClass
	public static void init() {
		ConstraintAnnotationDescriptor.Builder<Digits> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Digits.class );
		descriptorBuilder.setAttribute( "integer", 5 );
		descriptorBuilder.setAttribute( "fraction", 2 );
		descriptorBuilder.setMessage( "{validator.digits}" );
		Digits p = descriptorBuilder.build().getAnnotation();

		constraint = new DigitsValidatorForCharSequence();
		constraint.initialize( p );
	}

	@BeforeMethod
	public void setUp() throws Exception {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Digits.class );
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
		descriptorBuilder.setAttribute( "integer", -1 );
		descriptorBuilder.setAttribute( "fraction", 1 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForCharSequence constraint = new DigitsValidatorForCharSequence();
		constraint.initialize( p );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeFractionLength() {
		descriptorBuilder.setAttribute( "integer", 1 );
		descriptorBuilder.setAttribute( "fraction", -1 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForCharSequence constraint = new DigitsValidatorForCharSequence();
		constraint.initialize( p );
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void testIsValidCharSequence() {
		assertTrue( constraint.isValid( new MyCustomStringImpl( "500.2" ), null ) );
	}
}
