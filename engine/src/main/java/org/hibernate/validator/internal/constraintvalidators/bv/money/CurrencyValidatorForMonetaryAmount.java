/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.money;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Currency;

/**
 * Check that the validated {@link MonetaryAmount} is in the right {@link CurrencyUnit}.
 *
 * @author Guillaume Smet
 */
public class CurrencyValidatorForMonetaryAmount implements ConstraintValidator<Currency, MonetaryAmount> {

	private List<CurrencyUnit> acceptedCurrencies;

	@Override
	public void initialize(Currency currency) {
		List<CurrencyUnit> acceptedCurrencies = new ArrayList<CurrencyUnit>();
		for ( String currencyCode : currency.value() ) {
			acceptedCurrencies.add( Monetary.getCurrency( currencyCode ) );
		}
		this.acceptedCurrencies = Collections.unmodifiableList( acceptedCurrencies );
	}

	@Override
	public boolean isValid(MonetaryAmount value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		return acceptedCurrencies.contains( value.getCurrency() );
	}

}
