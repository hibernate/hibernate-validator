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

/**
 * @author Marko Bekhta
 */
public class BaseMinMaxValidatorForNumberTest {

	protected void testNumberValidator(ConstraintValidator<?, Number> constraint, boolean inclusive, boolean isMax) {
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
		assertEquals( constraint.isValid( 10, null ), isMax );
		assertEquals( constraint.isValid( b, null ), isMax );
		assertEquals( constraint.isValid( 14.99, null ), isMax );
		assertEquals( constraint.isValid( -14.99, null ), isMax );
		assertEquals( constraint.isValid( bWrapper, null ), !isMax );
		assertEquals( constraint.isValid( 20, null ), !isMax );
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
		assertEquals( constraint.isValid( BigDecimal.valueOf( -156000000000.0 ), null ), isMax );
		assertEquals( constraint.isValid( BigDecimal.valueOf( 156000000000.0 ), null ), !isMax );
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
		assertEquals( constraint.isValid( BigInteger.valueOf( -1560000000 ), null ), isMax );
		assertEquals( constraint.isValid( BigInteger.valueOf( 1560000000 ), null ), !isMax );
	}

	protected void testValidatorLong(ConstraintValidator<?, Long> constraint, boolean inclusive, boolean isMax) {
		if ( inclusive ) {
			assertTrue( constraint.isValid( 15L, null ) );
		}
		else {
			assertFalse( constraint.isValid( 15L, null ) );
		}

		assertTrue( constraint.isValid( null, null ) );
		assertEquals( constraint.isValid( -1560000000L, null ), isMax );
		assertEquals( constraint.isValid( 1560000000L, null ), !isMax );
	}

	protected void testValidatorDouble(ConstraintValidator<?, Double> constraint, boolean inclusive, boolean isMax) {
		if ( inclusive ) {
			assertTrue( constraint.isValid( 15D, null ) );
		}
		else {
			assertFalse( constraint.isValid( 15D, null ) );
		}

		assertTrue( constraint.isValid( null, null ) );
		assertEquals( constraint.isValid( 14.99, null ), isMax );
		assertEquals( constraint.isValid( -14.99, null ), isMax );
		assertEquals( constraint.isValid( -1560000000D, null ), isMax );
		assertEquals( constraint.isValid( Double.NEGATIVE_INFINITY, null ), isMax );
		assertEquals( constraint.isValid( 1560000000D, null ), !isMax );
		assertFalse( constraint.isValid( Double.NaN, null ) );
		assertEquals( constraint.isValid( Double.POSITIVE_INFINITY, null ), !isMax );
	}

	protected void testValidatorFloat(ConstraintValidator<?, Float> constraint, boolean inclusive, boolean isMax) {
		if ( inclusive ) {
			assertTrue( constraint.isValid( 15F, null ) );
		}
		else {
			assertFalse( constraint.isValid( 15F, null ) );
		}

		assertTrue( constraint.isValid( null, null ) );
		assertEquals( constraint.isValid( -1560000000F, null ), isMax );
		assertEquals( constraint.isValid( Float.NEGATIVE_INFINITY, null ), isMax );
		assertEquals( constraint.isValid( 1560000000F, null ), !isMax );
		assertFalse( constraint.isValid( Float.NaN, null ) );
		assertEquals( constraint.isValid( Float.POSITIVE_INFINITY, null ), !isMax );
	}
}
