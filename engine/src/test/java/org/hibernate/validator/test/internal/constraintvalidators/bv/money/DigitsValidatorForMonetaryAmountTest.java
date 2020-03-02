/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.money;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import jakarta.validation.constraints.Digits;

import org.hibernate.validator.internal.constraintvalidators.bv.money.DigitsValidatorForMonetaryAmount;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;

import org.javamoney.moneta.Money;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Dario Seidl
 */
@TestForIssue(jiraKey = "HV-1723")
public class DigitsValidatorForMonetaryAmountTest {

	private ConstraintAnnotationDescriptor.Builder<Digits> descriptorBuilder;

	@BeforeMethod
	public void setUp() {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Digits.class );
		descriptorBuilder.setMessage( "{validator.digits}" );
	}

	@Test
	public void testIsValid() {
		descriptorBuilder.setAttribute( "integer", 5 );
		descriptorBuilder.setAttribute( "fraction", 2 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForMonetaryAmount constraint = new DigitsValidatorForMonetaryAmount();
		constraint.initialize( p );

		CurrencyUnit currency = Monetary.getCurrency( "EUR" );

		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( Money.of( Byte.valueOf( "0" ), currency ), null ) );
		assertTrue( constraint.isValid( Money.of( Double.valueOf( "500.2" ), currency ), null ) );

		assertTrue( constraint.isValid( Money.of( new BigDecimal( "-12345.12" ), currency ), null ) );
		assertFalse( constraint.isValid( Money.of( new BigDecimal( "-123456.12" ), currency ), null ) );
		assertFalse( constraint.isValid( Money.of( new BigDecimal( "-123456.123" ), currency ), null ) );
		assertFalse( constraint.isValid( Money.of( new BigDecimal( "-12345.123" ), currency ), null ) );
		assertFalse( constraint.isValid( Money.of( new BigDecimal( "12345.123" ), currency ), null ) );

		assertTrue( constraint.isValid( Money.of( Float.valueOf( "-000000000.22" ), currency ), null ) );
		assertFalse( constraint.isValid( Money.of( Integer.valueOf( "256874" ), currency ), null ) );
		assertFalse( constraint.isValid( Money.of( Double.valueOf( "12.0001" ), currency ), null ) );
	}

	@Test
	public void testIsValidZeroLength() {
		descriptorBuilder.setAttribute( "integer", 0 );
		descriptorBuilder.setAttribute( "fraction", 0 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForMonetaryAmount constraint = new DigitsValidatorForMonetaryAmount();
		constraint.initialize( p );

		CurrencyUnit currency = Monetary.getCurrency( "EUR" );

		assertTrue( constraint.isValid( null, null ) );
		assertFalse( constraint.isValid( Money.of( Byte.valueOf( "0" ), currency ), null ) );
		assertFalse( constraint.isValid( Money.of( Double.valueOf( "500.2" ), currency ), null ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeIntegerLength() {
		descriptorBuilder.setAttribute( "integer", -1 );
		descriptorBuilder.setAttribute( "fraction", 1 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForMonetaryAmount constraint = new DigitsValidatorForMonetaryAmount();
		constraint.initialize( p );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeFractionLength() {
		descriptorBuilder.setAttribute( "integer", 1 );
		descriptorBuilder.setAttribute( "fraction", -1 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForMonetaryAmount constraint = new DigitsValidatorForMonetaryAmount();
		constraint.initialize( p );
	}

	@Test
	public void testTrailingZerosAreTrimmed() {
		descriptorBuilder.setAttribute( "integer", 12 );
		descriptorBuilder.setAttribute( "fraction", 3 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForMonetaryAmount constraint = new DigitsValidatorForMonetaryAmount();
		constraint.initialize( p );

		CurrencyUnit currency = Monetary.getCurrency( "EUR" );

		assertTrue( constraint.isValid( Money.of( 0.001d, currency ), null ) );
		assertTrue( constraint.isValid( Money.of( 0.00100d, currency ), null ) );
		assertFalse( constraint.isValid( Money.of( 0.0001d, currency ), null ) );
	}

}
