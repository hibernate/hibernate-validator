/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.money;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import javax.money.MonetaryAmount;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.constraints.DecimalMax;

import org.hibernate.validator.internal.constraintvalidators.bv.money.DecimalMaxValidatorForMonetaryAmount;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.javamoney.moneta.Money;
import org.testng.annotations.Test;

/**
 * @author Lukas Niemeier
 * @author Willi Schönborn
 */
public class DecimalMaxValidatorForMonetaryAmountTest {

	private final ConstraintValidator<DecimalMax, MonetaryAmount> unit = new DecimalMaxValidatorForMonetaryAmount();

	@Test
	public void nullIsValid() {
		unit.initialize( decimalMax( "0", true ) );

		assertTrue( unit.isValid( null, null ) );
	}

	@Test
	public void validIfLess() {
		unit.initialize( decimalMax( "0", true ) );

		assertTrue( unit.isValid( Money.of( -1, "EUR" ), null ) );
	}

	@Test
	public void invalidIfGreater() {
		unit.initialize( decimalMax( "0", true ) );

		assertFalse( unit.isValid( Money.of( 1, "EUR" ), null ) );
	}

	@Test
	public void validIfInclude() {
		unit.initialize( decimalMax( "0", true ) );

		assertTrue( unit.isValid( Money.of( 0, "EUR" ), null ) );
	}

	@Test
	public void invalidIfNotInclude() {
		unit.initialize( decimalMax( "0", false ) );

		assertFalse( unit.isValid( Money.of( 0, "EUR" ), null ) );
	}

	@Test
	public void validIfLessAndNotIncluded() {
		unit.initialize( decimalMax( "0", false ) );

		assertTrue( unit.isValid( Money.of( -1, "EUR" ), null ) );
	}

	private DecimalMax decimalMax(final String value, final boolean inclusive) {
		ConstraintAnnotationDescriptor.Builder<DecimalMax> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DecimalMax.class );
		descriptorBuilder.setAttribute( "value", value );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		return descriptorBuilder.build().getAnnotation();
	}

}
