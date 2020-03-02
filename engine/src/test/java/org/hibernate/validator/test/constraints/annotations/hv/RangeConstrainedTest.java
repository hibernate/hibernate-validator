/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import javax.money.MonetaryAmount;
import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.javamoney.moneta.Money;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class RangeConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testRangeNumber() {
		Foo foo = new Foo(
				"2", BigDecimal.ONE.add( BigDecimal.ONE ), BigInteger.valueOf( 2L ),
				2.0, 2.0f, 2L, 2, (byte) 2, (short) 2, 2, Money.of( 2, "UAH" )
		);
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testRangeInvalid() {
		Foo foo = new Foo(
				"0", BigDecimal.ZERO, BigInteger.ZERO,
				0.0, 0.0f, 0L, 0, (byte) 0, (short) 0, 0, Money.of( 0, "UAH" )
		);
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Range.class ).withProperty( "string" ),
				violationOf( Range.class ).withProperty( "bigDecimal" ),
				violationOf( Range.class ).withProperty( "bigInteger" ),
				violationOf( Range.class ).withProperty( "aDouble" ),
				violationOf( Range.class ).withProperty( "aFloat" ),
				violationOf( Range.class ).withProperty( "aLong" ),
				violationOf( Range.class ).withProperty( "anInt" ),
				violationOf( Range.class ).withProperty( "aByte" ),
				violationOf( Range.class ).withProperty( "aShort" ),
				violationOf( Range.class ).withProperty( "number" ),
				violationOf( Range.class ).withProperty( "amount" )
		);
	}

	private static class Foo {

		@Range(min = 1, max = 5)
		private final String string;

		@Range(min = 1, max = 5)
		private final BigDecimal bigDecimal;

		@Range(min = 1, max = 5)
		private final BigInteger bigInteger;

		@Range(min = 1, max = 5)
		private final double aDouble;

		@Range(min = 1, max = 5)
		private final float aFloat;

		@Range(min = 1, max = 5)
		private final long aLong;

		@Range(min = 1, max = 5)
		private final int anInt;

		@Range(min = 1, max = 5)
		private final byte aByte;

		@Range(min = 1, max = 5)
		private final short aShort;

		@Range(min = 1, max = 5)
		private final Number number;

		@Range(min = 1, max = 5)
		private final MonetaryAmount amount;

		public Foo(String string, BigDecimal bigDecimal,
				BigInteger bigInteger, double aDouble,
				float aFloat, long aLong, int anInt,
				byte aByte, short aShort,
				Number number, MonetaryAmount amount) {
			this.string = string;
			this.bigDecimal = bigDecimal;
			this.bigInteger = bigInteger;
			this.aDouble = aDouble;
			this.aFloat = aFloat;
			this.aLong = aLong;
			this.anInt = anInt;
			this.aByte = aByte;
			this.aShort = aShort;
			this.number = number;
			this.amount = amount;
		}
	}
}
