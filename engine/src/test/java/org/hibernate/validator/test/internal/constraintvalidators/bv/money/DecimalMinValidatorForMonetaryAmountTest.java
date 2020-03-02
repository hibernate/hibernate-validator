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
import jakarta.validation.constraints.DecimalMin;

import org.hibernate.validator.internal.constraintvalidators.bv.money.DecimalMinValidatorForMonetaryAmount;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.javamoney.moneta.Money;
import org.testng.annotations.Test;

/**
 * @author Lukas Niemeier
 * @author Willi Schönborn
 */
public class DecimalMinValidatorForMonetaryAmountTest {

	private final ConstraintValidator<DecimalMin, MonetaryAmount> unit = new DecimalMinValidatorForMonetaryAmount();

	@Test
	public void nullIsValid() {
		unit.initialize( decimalMin( "0", true ) );

		assertTrue( unit.isValid( null, null ) );
	}

	@Test
	public void invalidIfLess() {
		unit.initialize( decimalMin( "0", true ) );

		assertFalse( unit.isValid( Money.of( -1, "EUR" ), null ) );
	}

	@Test
	public void validIfGreater() {
		unit.initialize( decimalMin( "0", true ) );

		assertTrue( unit.isValid( Money.of( 1, "EUR" ), null ) );
	}

	@Test
	public void validIfInclude() {
		unit.initialize( decimalMin( "0", true ) );

		assertTrue( unit.isValid( Money.of( 0, "EUR" ), null ) );
	}

	@Test
	public void invalidIfNotInclude() {
		unit.initialize( decimalMin( "0", false ) );

		assertFalse( unit.isValid( Money.of( 0, "EUR" ), null ) );
	}

	@Test
	public void validIfGreaterAndNotIncluded() {
		unit.initialize( decimalMin( "0", false ) );

		assertTrue( unit.isValid( Money.of( 1, "EUR" ), null ) );
	}

	private DecimalMin decimalMin(final String value, final boolean inclusive) {
		ConstraintAnnotationDescriptor.Builder<DecimalMin> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DecimalMin.class );
		descriptorBuilder.setAttribute( "value", value );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		return descriptorBuilder.build().getAnnotation();
	}

}
