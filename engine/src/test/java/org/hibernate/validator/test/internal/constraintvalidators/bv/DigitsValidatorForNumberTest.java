/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import org.hibernate.validator.internal.constraintvalidators.bv.DigitsValidatorForNumber;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.testng.annotations.Test;

import javax.validation.constraints.Digits;
import java.math.BigDecimal;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Alaa Nassef
 * @author Hardy Ferentschik
 */
public class DigitsValidatorForNumberTest {

	@Test
	public void testIsValid() {

		AnnotationDescriptor<Digits> descriptor = new AnnotationDescriptor<Digits>( Digits.class );
		descriptor.setValue( "integer", 5 );
		descriptor.setValue( "fraction", 2 );
		descriptor.setValue( "message", "{validator.digits}" );
		Digits p = AnnotationFactory.create( descriptor );

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

		AnnotationDescriptor<Digits> descriptor = new AnnotationDescriptor<Digits>( Digits.class );
		descriptor.setValue( "integer", 0 );
		descriptor.setValue( "fraction", 0 );
		descriptor.setValue( "message", "{validator.digits}" );
		Digits p = AnnotationFactory.create( descriptor );

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );


		assertTrue( constraint.isValid( null, null ) );
		assertFalse( constraint.isValid( Byte.valueOf( "0" ), null ) );
		assertFalse( constraint.isValid( Double.valueOf( "500.2" ), null ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeIntegerLength() {

		AnnotationDescriptor<Digits> descriptor = new AnnotationDescriptor<Digits>( Digits.class );
		descriptor.setValue( "integer", -1 );
		descriptor.setValue( "fraction", 1 );
		descriptor.setValue( "message", "{validator.digits}" );
		Digits p = AnnotationFactory.create( descriptor );

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeFractionLength() {

		AnnotationDescriptor<Digits> descriptor = new AnnotationDescriptor<Digits>( Digits.class );
		descriptor.setValue( "integer", 1 );
		descriptor.setValue( "fraction", -1 );
		descriptor.setValue( "message", "{validator.digits}" );
		Digits p = AnnotationFactory.create( descriptor );

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );
	}

	@Test
	public void testTrailingZerosAreTrimmed() {
		AnnotationDescriptor<Digits> descriptor = new AnnotationDescriptor<Digits>( Digits.class );
		descriptor.setValue( "integer", 12 );
		descriptor.setValue( "fraction", 3 );
		descriptor.setValue( "message", "{validator.digits}" );
		Digits p = AnnotationFactory.create( descriptor );

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );

		assertTrue( constraint.isValid( 0.001d, null ) );
		assertTrue( constraint.isValid( 0.00100d, null ) );
		assertFalse( constraint.isValid( 0.0001d, null ) );
	}

}
