/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForShort;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public class SignValidatorForNumberTest {

	@Test
	public void testPositiveValidator() {
		testPositive( new ConstraintAnnotationDescriptor.Builder<>( Positive.class ).build().getAnnotation() );
	}

	@Test
	public void testPositiveOrZeroValidator() {
		testPositiveOrZero( new ConstraintAnnotationDescriptor.Builder<>( PositiveOrZero.class ).build().getAnnotation() );
	}

	@Test
	public void testNegativeValidator() {
		testNegative( new ConstraintAnnotationDescriptor.Builder<>( Negative.class ).build().getAnnotation() );
	}

	@Test
	public void testNegativeOrZeroValidator() {
		testNegativeOrZero( new ConstraintAnnotationDescriptor.Builder<>( NegativeOrZero.class ).build().getAnnotation() );
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void testNegative(Negative m) {
		ConstraintValidator validator = new NegativeValidatorForNumber();
		validator.initialize( m );
		testSignNumber( validator, true, false );

		validator = new NegativeValidatorForBigDecimal();
		validator.initialize( m );
		testSignBigDecimal( validator, true, false );

		validator = new NegativeValidatorForBigInteger();
		validator.initialize( m );
		testSignBigInteger( validator, true, false );

		validator = new NegativeValidatorForLong();
		validator.initialize( m );
		testSignLong( validator, true, false );

		validator = new NegativeValidatorForFloat();
		validator.initialize( m );
		testSignFloat( validator, true, false );

		validator = new NegativeValidatorForDouble();
		validator.initialize( m );
		testSignDouble( validator, true, false );

		validator = new NegativeValidatorForShort();
		validator.initialize( m );
		testSignShort( validator, true, false );

		validator = new NegativeValidatorForByte();
		validator.initialize( m );
		testSignByte( validator, true, false );

		validator = new NegativeValidatorForInteger();
		validator.initialize( m );
		testSignInteger( validator, true, false );
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void testNegativeOrZero(NegativeOrZero m) {
		ConstraintValidator validator = new NegativeOrZeroValidatorForNumber();
		validator.initialize( m );
		testSignNumber( validator, false, false );

		validator = new NegativeOrZeroValidatorForBigDecimal();
		validator.initialize( m );
		testSignBigDecimal( validator, false, false );

		validator = new NegativeOrZeroValidatorForBigInteger();
		validator.initialize( m );
		testSignBigInteger( validator, false, false );

		validator = new NegativeOrZeroValidatorForLong();
		validator.initialize( m );
		testSignLong( validator, false, false );

		validator = new NegativeOrZeroValidatorForFloat();
		validator.initialize( m );
		testSignFloat( validator, false, false );

		validator = new NegativeOrZeroValidatorForDouble();
		validator.initialize( m );
		testSignDouble( validator, false, false );

		validator = new NegativeOrZeroValidatorForShort();
		validator.initialize( m );
		testSignShort( validator, false, false );

		validator = new NegativeOrZeroValidatorForByte();
		validator.initialize( m );
		testSignByte( validator, false, false );

		validator = new NegativeOrZeroValidatorForInteger();
		validator.initialize( m );
		testSignInteger( validator, false, false );
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void testPositive(Positive m) {
		ConstraintValidator validator = new PositiveValidatorForNumber();
		validator.initialize( m );
		testSignNumber( validator, true, true );

		validator = new PositiveValidatorForBigDecimal();
		validator.initialize( m );
		testSignBigDecimal( validator, true, true );

		validator = new PositiveValidatorForBigInteger();
		validator.initialize( m );
		testSignBigInteger( validator, true, true );

		validator = new PositiveValidatorForLong();
		validator.initialize( m );
		testSignLong( validator, true, true );

		validator = new PositiveValidatorForFloat();
		validator.initialize( m );
		testSignFloat( validator, true, true );

		validator = new PositiveValidatorForDouble();
		validator.initialize( m );
		testSignDouble( validator, true, true );

		validator = new PositiveValidatorForShort();
		validator.initialize( m );
		testSignShort( validator, true, true );

		validator = new PositiveValidatorForByte();
		validator.initialize( m );
		testSignByte( validator, true, true );

		validator = new PositiveValidatorForInteger();
		validator.initialize( m );
		testSignInteger( validator, true, true );
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void testPositiveOrZero(PositiveOrZero m) {
		ConstraintValidator validator = new PositiveOrZeroValidatorForNumber();
		validator.initialize( m );
		testSignNumber( validator, false, true );

		validator = new PositiveOrZeroValidatorForBigDecimal();
		validator.initialize( m );
		testSignBigDecimal( validator, false, true );

		validator = new PositiveOrZeroValidatorForBigInteger();
		validator.initialize( m );
		testSignBigInteger( validator, false, true );

		validator = new PositiveOrZeroValidatorForLong();
		validator.initialize( m );
		testSignLong( validator, false, true );

		validator = new PositiveOrZeroValidatorForFloat();
		validator.initialize( m );
		testSignFloat( validator, false, true );

		validator = new PositiveOrZeroValidatorForDouble();
		validator.initialize( m );
		testSignDouble( validator, false, true );

		validator = new PositiveOrZeroValidatorForShort();
		validator.initialize( m );
		testSignShort( validator, false, true );

		validator = new PositiveOrZeroValidatorForByte();
		validator.initialize( m );
		testSignByte( validator, false, true );

		validator = new PositiveOrZeroValidatorForInteger();
		validator.initialize( m );
		testSignInteger( validator, false, true );
	}

	private void testSignShort(ConstraintValidator<?, Number> validator, boolean strict, boolean positive) {
		assertTrue( validator.isValid( null, null ) );
		assertEquals( validator.isValid( (short) 0, null ), !strict );
		assertEquals( validator.isValid( (short) 1, null ), positive );
		assertEquals( validator.isValid( (short) -1, null ), !positive );
		assertEquals( validator.isValid( (short) 10.0, null ), positive );
		assertEquals( validator.isValid( (short) -10.0, null ), !positive );
	}

	private void testSignByte(ConstraintValidator<?, Number> validator, boolean strict, boolean positive) {
		assertTrue( validator.isValid( null, null ) );
		assertEquals( validator.isValid( (byte) 0, null ), !strict );
		assertEquals( validator.isValid( (byte) 1, null ), positive );
		assertEquals( validator.isValid( (byte) -1, null ), !positive );
		assertEquals( validator.isValid( (byte) 10.0, null ), positive );
		assertEquals( validator.isValid( (byte) -10.0, null ), !positive );
	}

	private void testSignInteger(ConstraintValidator<?, Number> validator, boolean strict, boolean positive) {
		assertTrue( validator.isValid( null, null ) );
		assertEquals( validator.isValid( 0, null ), !strict );
		assertEquals( validator.isValid( 1, null ), positive );
		assertEquals( validator.isValid( -1, null ), !positive );
		assertEquals( validator.isValid( 10, null ), positive );
		assertEquals( validator.isValid( -10, null ), !positive );
	}

	private void testSignNumber(ConstraintValidator<?, Number> validator, boolean strict, boolean positive) {
		assertTrue( validator.isValid( null, null ) );
		assertEquals( validator.isValid( 0, null ), !strict );
		assertEquals( validator.isValid( 1, null ), positive );
		assertEquals( validator.isValid( -1, null ), !positive );
		assertEquals( validator.isValid( 10.0, null ), positive );
		assertEquals( validator.isValid( -10.0, null ), !positive );
	}

	private void testSignBigDecimal(ConstraintValidator<?, BigDecimal> validator, boolean strict, boolean positive) {
		assertTrue( validator.isValid( null, null ) );
		assertEquals( validator.isValid( BigDecimal.ZERO, null ), !strict );
		assertEquals( validator.isValid( BigDecimal.ONE, null ), positive );
		assertEquals( validator.isValid( BigDecimal.ONE.negate(), null ), !positive );
		assertEquals( validator.isValid( BigDecimal.TEN, null ), positive );
		assertEquals( validator.isValid( BigDecimal.TEN.negate(), null ), !positive );
	}

	private void testSignBigInteger(ConstraintValidator<?, BigInteger> validator, boolean strict, boolean positive) {
		assertTrue( validator.isValid( null, null ) );
		assertEquals( validator.isValid( BigInteger.ZERO, null ), !strict );
		assertEquals( validator.isValid( BigInteger.ONE, null ), positive );
		assertEquals( validator.isValid( BigInteger.ONE.negate(), null ), !positive );
		assertEquals( validator.isValid( BigInteger.TEN, null ), positive );
		assertEquals( validator.isValid( BigInteger.TEN.negate(), null ), !positive );
	}

	private void testSignLong(ConstraintValidator<?, Long> validator, boolean strict, boolean positive) {
		assertTrue( validator.isValid( null, null ) );
		assertEquals( validator.isValid( 0L, null ), !strict );
		assertEquals( validator.isValid( 1L, null ), positive );
		assertEquals( validator.isValid( -1L, null ), !positive );
		assertEquals( validator.isValid( 10L, null ), positive );
		assertEquals( validator.isValid( -10L, null ), !positive );
	}

	private void testSignDouble(ConstraintValidator<?, Double> validator, boolean strict, boolean positive) {
		assertTrue( validator.isValid( null, null ) );
		assertEquals( validator.isValid( 0D, null ), !strict );
		assertEquals( validator.isValid( 1D, null ), positive );
		assertEquals( validator.isValid( -1D, null ), !positive );
		assertEquals( validator.isValid( 10D, null ), positive );
		assertEquals( validator.isValid( -10D, null ), !positive );
		assertEquals( validator.isValid( Double.POSITIVE_INFINITY, null ), positive );
		assertEquals( validator.isValid( Double.NEGATIVE_INFINITY, null ), !positive );
		assertFalse( validator.isValid( Double.NaN, null ) );
	}

	private void testSignFloat(ConstraintValidator<?, Float> validator, boolean strict, boolean positive) {
		assertTrue( validator.isValid( null, null ) );
		assertEquals( validator.isValid( 0F, null ), !strict );
		assertEquals( validator.isValid( 1F, null ), positive );
		assertEquals( validator.isValid( -1F, null ), !positive );
		assertEquals( validator.isValid( 10F, null ), positive );
		assertEquals( validator.isValid( -10F, null ), !positive );
		assertEquals( validator.isValid( Float.POSITIVE_INFINITY, null ), positive );
		assertEquals( validator.isValid( Float.NEGATIVE_INFINITY, null ), !positive );
		assertFalse( validator.isValid( Float.NaN, null ) );
	}
}
