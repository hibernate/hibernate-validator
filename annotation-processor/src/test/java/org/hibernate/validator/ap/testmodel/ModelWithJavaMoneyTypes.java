/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel;

import javax.money.MonetaryAmount;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import org.hibernate.validator.constraints.Currency;

public class ModelWithJavaMoneyTypes {

	@Currency("EUR")
	@Negative
	@NegativeOrZero
	public MonetaryAmount monetaryAmountEuro;

	@DecimalMax("1000.00")
	@DecimalMin("0.00")
	@Positive
	@PositiveOrZero
	@Digits(integer = 6, fraction = 2)
	public MonetaryAmount monetaryAmount;

	@Max(1000L)
	@Min(1L)
	public MonetaryAmount anotherMonetaryAmount;

}
