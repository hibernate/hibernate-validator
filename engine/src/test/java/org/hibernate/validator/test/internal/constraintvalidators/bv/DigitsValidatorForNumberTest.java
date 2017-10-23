/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;

import org.hibernate.validator.internal.constraintvalidators.bv.DigitsValidatorForNumber;
import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;
import org.testng.annotations.Test;

/**
 * @author Alaa Nassef
 * @author Hardy Ferentschik
 */
public class DigitsValidatorForNumberTest {

	@Test
	public void testIsValid() {

		AnnotationDescriptor.Builder<Digits> descriptorBuilder = new AnnotationDescriptor.Builder<>( Digits.class );
		descriptorBuilder.setAttribute( "integer", 5 );
		descriptorBuilder.setAttribute( "fraction", 2 );
		descriptorBuilder.setAttribute( "message", "{validator.digits}" );
		Digits p = descriptorBuilder.build().annotation();

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );


		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( Byte.valueOf( "0" ), null ) );
		assertTrue( constraint.isValid( Double.valueOf( "500.2" ), null ) );

		assertTrue( constraint.isValid( new BigDecimal( "-12345.12" ), null ) );
		assertFalse( constraint.isValid( new BigDecimal( "-123456.12" ), null ) );
		assertFalse( constraint.isValid( new BigDecimal( "-123456.123" ), null ) );
		assertFalse( constraint.isValid( new BigDecimal( "-12345.123" ), null ) );
		assertFalse( constraint.isValid( new BigDecimal( "12345.123" ), null ) );

		assertTrue( constraint.isValid( Float.valueOf( "-000000000.22" ), null ) );
		assertFalse( constraint.isValid( Integer.valueOf( "256874" ), null ) );
		assertFalse( constraint.isValid( Double.valueOf( "12.0001" ), null ) );
	}

	@Test
	public void testIsValidZeroLength() {

		AnnotationDescriptor.Builder<Digits> descriptorBuilder = new AnnotationDescriptor.Builder<>( Digits.class );
		descriptorBuilder.setAttribute( "integer", 0 );
		descriptorBuilder.setAttribute( "fraction", 0 );
		descriptorBuilder.setAttribute( "message", "{validator.digits}" );
		Digits p = descriptorBuilder.build().annotation();

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );


		assertTrue( constraint.isValid( null, null ) );
		assertFalse( constraint.isValid( Byte.valueOf( "0" ), null ) );
		assertFalse( constraint.isValid( Double.valueOf( "500.2" ), null ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeIntegerLength() {

		AnnotationDescriptor.Builder<Digits> descriptorBuilder = new AnnotationDescriptor.Builder<>( Digits.class );
		descriptorBuilder.setAttribute( "integer", -1 );
		descriptorBuilder.setAttribute( "fraction", 1 );
		descriptorBuilder.setAttribute( "message", "{validator.digits}" );
		Digits p = descriptorBuilder.build().annotation();

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeFractionLength() {

		AnnotationDescriptor.Builder<Digits> descriptorBuilder = new AnnotationDescriptor.Builder<>( Digits.class );
		descriptorBuilder.setAttribute( "integer", 1 );
		descriptorBuilder.setAttribute( "fraction", -1 );
		descriptorBuilder.setAttribute( "message", "{validator.digits}" );
		Digits p = descriptorBuilder.build().annotation();

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );
	}

	@Test
	public void testTrailingZerosAreTrimmed() {
		AnnotationDescriptor.Builder<Digits> descriptorBuilder = new AnnotationDescriptor.Builder<>( Digits.class );
		descriptorBuilder.setAttribute( "integer", 12 );
		descriptorBuilder.setAttribute( "fraction", 3 );
		descriptorBuilder.setAttribute( "message", "{validator.digits}" );
		Digits p = descriptorBuilder.build().annotation();

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );

		assertTrue( constraint.isValid( 0.001d, null ) );
		assertTrue( constraint.isValid( 0.00100d, null ) );
		assertFalse( constraint.isValid( 0.0001d, null ) );
	}

}
