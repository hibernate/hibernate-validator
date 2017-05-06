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
import javax.validation.ConstraintValidator;
import javax.validation.constraints.Negative;
import javax.validation.constraints.Positive;

import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.BaseNegativeValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.BasePositiveValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForShort;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class SignValidatorForNumberTest {

	@Test
	public void testPositiveValidator() {
		AnnotationDescriptor<Positive> descriptor = new AnnotationDescriptor<>( Positive.class );

		testPositive( AnnotationFactory.create( descriptor ), false );

		descriptor.setValue( "strict", true );
		testPositive( AnnotationFactory.create( descriptor ), true );
	}

	@Test
	public void testNegativeValidator() {
		AnnotationDescriptor<Negative> descriptor = new AnnotationDescriptor<>( Negative.class );

		testNegative( AnnotationFactory.create( descriptor ), false );

		descriptor.setValue( "strict", true );
		testNegative( AnnotationFactory.create( descriptor ), true );
	}

	private void testNegative(Negative m, boolean strict) {
		BaseNegativeValidator constraint = new NegativeValidatorForNumber();
		constraint.initialize( m );
		testSignNumber( constraint, strict, false );

		constraint = new NegativeValidatorForBigDecimal();
		constraint.initialize( m );
		testSignBigDecimal( constraint, strict, false );

		constraint = new NegativeValidatorForBigInteger();
		constraint.initialize( m );
		testSignBigInteger( constraint, strict, false );

		constraint = new NegativeValidatorForLong();
		constraint.initialize( m );
		testSignLong( constraint, strict, false );

		constraint = new NegativeValidatorForFloat();
		constraint.initialize( m );
		testSignFloat( constraint, strict, false );

		constraint = new NegativeValidatorForDouble();
		constraint.initialize( m );
		testSignDouble( constraint, strict, false );

		constraint = new NegativeValidatorForShort();
		constraint.initialize( m );
		testSignShort( constraint, strict, false );

		constraint = new NegativeValidatorForByte();
		constraint.initialize( m );
		testSignByte( constraint, strict, false );

		constraint = new NegativeValidatorForInteger();
		constraint.initialize( m );
		testSignInteger( constraint, strict, false );
	}

	private void testPositive(Positive m, boolean strict) {
		BasePositiveValidator constraint = new PositiveValidatorForNumber();
		constraint.initialize( m );
		testSignNumber( constraint, strict, true );

		constraint = new PositiveValidatorForBigDecimal();
		constraint.initialize( m );
		testSignBigDecimal( constraint, strict, true );

		constraint = new PositiveValidatorForBigInteger();
		constraint.initialize( m );
		testSignBigInteger( constraint, strict, true );

		constraint = new PositiveValidatorForLong();
		constraint.initialize( m );
		testSignLong( constraint, strict, true );

		constraint = new PositiveValidatorForFloat();
		constraint.initialize( m );
		testSignFloat( constraint, strict, true );

		constraint = new PositiveValidatorForDouble();
		constraint.initialize( m );
		testSignDouble( constraint, strict, true );

		constraint = new PositiveValidatorForShort();
		constraint.initialize( m );
		testSignShort( constraint, strict, true );

		constraint = new PositiveValidatorForByte();
		constraint.initialize( m );
		testSignByte( constraint, strict, true );

		constraint = new PositiveValidatorForInteger();
		constraint.initialize( m );
		testSignInteger( constraint, strict, true );
	}

	private void testSignShort(ConstraintValidator<?, Number> constraint, boolean strict, boolean positive) {
		assertTrue( constraint.isValid( null, null ) );
		assertEquals( constraint.isValid( (short) 0, null ), !strict );
		assertEquals( constraint.isValid( (short) 1, null ), positive );
		assertEquals( constraint.isValid( (short) -1, null ), !positive );
		assertEquals( constraint.isValid( (short) 10.0, null ), positive );
		assertEquals( constraint.isValid( (short) -10.0, null ), !positive );
	}

	private void testSignByte(ConstraintValidator<?, Number> constraint, boolean strict, boolean positive) {
		assertTrue( constraint.isValid( null, null ) );
		assertEquals( constraint.isValid( (byte) 0, null ), !strict );
		assertEquals( constraint.isValid( (byte) 1, null ), positive );
		assertEquals( constraint.isValid( (byte) -1, null ), !positive );
		assertEquals( constraint.isValid( (byte) 10.0, null ), positive );
		assertEquals( constraint.isValid( (byte) -10.0, null ), !positive );
	}

	private void testSignInteger(ConstraintValidator<?, Number> constraint, boolean strict, boolean positive) {
		assertTrue( constraint.isValid( null, null ) );
		assertEquals( constraint.isValid( 0, null ), !strict );
		assertEquals( constraint.isValid( 1, null ), positive );
		assertEquals( constraint.isValid( -1, null ), !positive );
		assertEquals( constraint.isValid( 10, null ), positive );
		assertEquals( constraint.isValid( -10, null ), !positive );
	}

	private void testSignNumber(ConstraintValidator<?, Number> constraint, boolean strict, boolean positive) {
		assertTrue( constraint.isValid( null, null ) );
		assertEquals( constraint.isValid( 0, null ), !strict );
		assertEquals( constraint.isValid( 1, null ), positive );
		assertEquals( constraint.isValid( -1, null ), !positive );
		assertEquals( constraint.isValid( 10.0, null ), positive );
		assertEquals( constraint.isValid( -10.0, null ), !positive );
	}

	private void testSignBigDecimal(ConstraintValidator<?, BigDecimal> constraint, boolean strict, boolean positive) {
		assertTrue( constraint.isValid( null, null ) );
		assertEquals( constraint.isValid( BigDecimal.ZERO, null ), !strict );
		assertEquals( constraint.isValid( BigDecimal.ONE, null ), positive );
		assertEquals( constraint.isValid( BigDecimal.ONE.negate(), null ), !positive );
		assertEquals( constraint.isValid( BigDecimal.TEN, null ), positive );
		assertEquals( constraint.isValid( BigDecimal.TEN.negate(), null ), !positive );
	}

	private void testSignBigInteger(ConstraintValidator<?, BigInteger> constraint, boolean strict, boolean positive) {
		assertTrue( constraint.isValid( null, null ) );
		assertEquals( constraint.isValid( BigInteger.ZERO, null ), !strict );
		assertEquals( constraint.isValid( BigInteger.ONE, null ), positive );
		assertEquals( constraint.isValid( BigInteger.ONE.negate(), null ), !positive );
		assertEquals( constraint.isValid( BigInteger.TEN, null ), positive );
		assertEquals( constraint.isValid( BigInteger.TEN.negate(), null ), !positive );
	}

	private void testSignLong(ConstraintValidator<?, Long> constraint, boolean strict, boolean positive) {
		assertTrue( constraint.isValid( null, null ) );
		assertEquals( constraint.isValid( 0L, null ), !strict );
		assertEquals( constraint.isValid( 1L, null ), positive );
		assertEquals( constraint.isValid( -1L, null ), !positive );
		assertEquals( constraint.isValid( 10L, null ), positive );
		assertEquals( constraint.isValid( -10L, null ), !positive );
	}

	private void testSignDouble(ConstraintValidator<?, Double> constraint, boolean strict, boolean positive) {
		assertTrue( constraint.isValid( null, null ) );
		assertEquals( constraint.isValid( 0D, null ), !strict );
		assertEquals( constraint.isValid( 1D, null ), positive );
		assertEquals( constraint.isValid( -1D, null ), !positive );
		assertEquals( constraint.isValid( 10D, null ), positive );
		assertEquals( constraint.isValid( -10D, null ), !positive );
		assertEquals( constraint.isValid( Double.POSITIVE_INFINITY, null ), positive );
		assertEquals( constraint.isValid( Double.NEGATIVE_INFINITY, null ), !positive );
		assertFalse( constraint.isValid( Double.NaN, null ) );
	}

	private void testSignFloat(ConstraintValidator<?, Float> constraint, boolean strict, boolean positive) {
		assertTrue( constraint.isValid( null, null ) );
		assertEquals( constraint.isValid( 0F, null ), !strict );
		assertEquals( constraint.isValid( 1F, null ), positive );
		assertEquals( constraint.isValid( -1F, null ), !positive );
		assertEquals( constraint.isValid( 10F, null ), positive );
		assertEquals( constraint.isValid( -10F, null ), !positive );
		assertEquals( constraint.isValid( Float.POSITIVE_INFINITY, null ), positive );
		assertEquals( constraint.isValid( Float.NEGATIVE_INFINITY, null ), !positive );
		assertFalse( constraint.isValid( Float.NaN, null ) );
	}
}
