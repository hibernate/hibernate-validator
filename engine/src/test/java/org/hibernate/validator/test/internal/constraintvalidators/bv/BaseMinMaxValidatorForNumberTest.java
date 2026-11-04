/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;

import jakarta.validation.ConstraintValidator;

/**
 * @author Marko Bekhta
 */
public abstract class BaseMinMaxValidatorForNumberTest {

	protected <A extends Annotation> void testNumberValidator(ConstraintValidator<A, Number> constraint, boolean inclusive, boolean isMax) {
		byte b = 1;
		Byte bWrapper = 127;
		if ( inclusive ) {
			assertTrue( constraint.isValid( 15L, null ) );
			assertTrue( constraint.isValid( 15, null ) );
			assertTrue( constraint.isValid( 15.0, null ) );
		}
		else {
			assertFalse( constraint.isValid( 15L, null ) );
			assertFalse( constraint.isValid( 15, null ) );
			assertFalse( constraint.isValid( 15.0, null ) );
		}

		assertTrue( constraint.isValid( null, null ) );
		assertEquals( isMax, constraint.isValid( 10, null ) );
		assertEquals( isMax, constraint.isValid( b, null ) );
		assertEquals( isMax, constraint.isValid( 14.99, null ) );
		assertEquals( isMax, constraint.isValid( -14.99, null ) );
		assertEquals( isMax, constraint.isValid( 14.99F, null ) );
		assertEquals( isMax, constraint.isValid( -14.99F, null ) );
		assertEquals( isMax, constraint.isValid( 14, null ) );
		assertEquals( !isMax, constraint.isValid( 16, null ) );
		assertEquals( isMax, constraint.isValid( (short) 14, null ) );
		assertEquals( !isMax, constraint.isValid( (short) 16, null ) );
		assertEquals( isMax, constraint.isValid( BigInteger.valueOf( 14L ), null ) );
		assertEquals( !isMax, constraint.isValid( BigInteger.valueOf( 16L ), null ) );
		assertEquals( isMax, constraint.isValid( BigDecimal.valueOf( 14L ), null ) );
		assertEquals( !isMax, constraint.isValid( BigDecimal.valueOf( 16L ), null ) );
		assertEquals( isMax, constraint.isValid( new BigDecimal( "14.99" ), null ) );
		assertEquals( !isMax, constraint.isValid( new BigDecimal( "15.001" ), null ) );
		assertEquals( !isMax, constraint.isValid( bWrapper, null ) );
		assertEquals( !isMax, constraint.isValid( 20, null ) );
	}

	protected void testValidatorBigDecimal(ConstraintValidator<?, BigDecimal> constraint, boolean inclusive, boolean isMax) {
		if ( inclusive ) {
			assertTrue( constraint.isValid( BigDecimal.valueOf( 15L ), null ) );
			assertTrue( constraint.isValid( BigDecimal.valueOf( 15 ), null ) );
			assertTrue( constraint.isValid( BigDecimal.valueOf( 15.0 ), null ) );
		}
		else {
			assertFalse( constraint.isValid( BigDecimal.valueOf( 15L ), null ) );
			assertFalse( constraint.isValid( BigDecimal.valueOf( 15 ), null ) );
			assertFalse( constraint.isValid( BigDecimal.valueOf( 15.0 ), null ) );
		}

		assertTrue( constraint.isValid( null, null ) );
		assertEquals( isMax, constraint.isValid( BigDecimal.valueOf( -156000000000.0 ), null ) );
		assertEquals( !isMax, constraint.isValid( BigDecimal.valueOf( 156000000000.0 ), null ) );
	}

	protected void testValidatorBigInteger(ConstraintValidator<?, BigInteger> constraint, boolean inclusive, boolean isMax) {
		if ( inclusive ) {
			assertTrue( constraint.isValid( BigInteger.valueOf( 15L ), null ) );
			assertTrue( constraint.isValid( BigInteger.valueOf( 15 ), null ) );
		}
		else {
			assertFalse( constraint.isValid( BigInteger.valueOf( 15L ), null ) );
			assertFalse( constraint.isValid( BigInteger.valueOf( 15 ), null ) );
		}

		assertTrue( constraint.isValid( null, null ) );
		assertEquals( isMax, constraint.isValid( BigInteger.valueOf( -1560000000 ), null ) );
		assertEquals( !isMax, constraint.isValid( BigInteger.valueOf( 1560000000 ), null ) );
	}

	protected void testValidatorByte(ConstraintValidator<?, Byte> constraint, boolean inclusive, boolean isMax) {
		if ( inclusive ) {
			assertTrue( constraint.isValid( (byte) 15, null ) );
		}
		else {
			assertFalse( constraint.isValid( (byte) 15, null ) );
		}

		assertTrue( constraint.isValid( null, null ) );
		assertEquals( isMax, constraint.isValid( (byte) 14, null ) );
		assertEquals( !isMax, constraint.isValid( (byte) 16, null ) );
		assertEquals( isMax, constraint.isValid( Byte.MIN_VALUE, null ) );
		assertEquals( !isMax, constraint.isValid( Byte.MAX_VALUE, null ) );
	}

	protected void testValidatorShort(ConstraintValidator<?, Short> constraint, boolean inclusive, boolean isMax) {
		if ( inclusive ) {
			assertTrue( constraint.isValid( (short) 15, null ) );
		}
		else {
			assertFalse( constraint.isValid( (short) 15, null ) );
		}

		assertTrue( constraint.isValid( null, null ) );
		assertEquals( isMax, constraint.isValid( (short) 14, null ) );
		assertEquals( !isMax, constraint.isValid( (short) 16, null ) );
		assertEquals( isMax, constraint.isValid( Short.MIN_VALUE, null ) );
		assertEquals( !isMax, constraint.isValid( Short.MAX_VALUE, null ) );
	}

	protected void testValidatorInteger(ConstraintValidator<?, Integer> constraint, boolean inclusive, boolean isMax) {
		if ( inclusive ) {
			assertTrue( constraint.isValid( 15, null ) );
		}
		else {
			assertFalse( constraint.isValid( 15, null ) );
		}

		assertTrue( constraint.isValid( null, null ) );
		assertEquals( isMax, constraint.isValid( 14, null ) );
		assertEquals( !isMax, constraint.isValid( 16, null ) );
		assertEquals( isMax, constraint.isValid( Integer.MIN_VALUE, null ) );
		assertEquals( !isMax, constraint.isValid( Integer.MAX_VALUE, null ) );
	}

	protected void testValidatorLong(ConstraintValidator<?, Long> constraint, boolean inclusive, boolean isMax) {
		if ( inclusive ) {
			assertTrue( constraint.isValid( 15L, null ) );
		}
		else {
			assertFalse( constraint.isValid( 15L, null ) );
		}

		assertTrue( constraint.isValid( null, null ) );
		assertEquals( isMax, constraint.isValid( -1560000000L, null ) );
		assertEquals( !isMax, constraint.isValid( 1560000000L, null ) );
	}

	protected void testValidatorDouble(ConstraintValidator<?, Double> constraint, boolean inclusive, boolean isMax) {
		if ( inclusive ) {
			assertTrue( constraint.isValid( 15D, null ) );
		}
		else {
			assertFalse( constraint.isValid( 15D, null ) );
		}

		assertTrue( constraint.isValid( null, null ) );
		assertEquals( isMax, constraint.isValid( 14.99, null ) );
		assertEquals( !isMax, constraint.isValid( 15.001, null ) );
		assertEquals( isMax, constraint.isValid( -14.99, null ) );
		assertEquals( isMax, constraint.isValid( -1560000000D, null ) );
		assertEquals( isMax, constraint.isValid( Double.NEGATIVE_INFINITY, null ) );
		assertEquals( !isMax, constraint.isValid( 1560000000D, null ) );
		assertFalse( constraint.isValid( Double.NaN, null ) );
		assertEquals( !isMax, constraint.isValid( Double.POSITIVE_INFINITY, null ) );
	}

	protected void testValidatorFloat(ConstraintValidator<?, Float> constraint, boolean inclusive, boolean isMax) {
		if ( inclusive ) {
			assertTrue( constraint.isValid( 15F, null ) );
		}
		else {
			assertFalse( constraint.isValid( 15F, null ) );
		}

		assertTrue( constraint.isValid( null, null ) );
		assertEquals( isMax, constraint.isValid( -1560000000F, null ) );
		assertEquals( isMax, constraint.isValid( Float.NEGATIVE_INFINITY, null ) );
		assertEquals( !isMax, constraint.isValid( 1560000000F, null ) );
		assertFalse( constraint.isValid( Float.NaN, null ) );
		assertEquals( !isMax, constraint.isValid( Float.POSITIVE_INFINITY, null ) );
	}
}
