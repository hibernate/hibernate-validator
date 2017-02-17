/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel;

import javax.money.MonetaryAmount;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.Currency;

public class ModelWithJavaMoneyTypes {

	@Currency("EUR")
	public MonetaryAmount monetaryAmountEuro;

	@DecimalMax("1000.00")
	@DecimalMin("0.00")
	public MonetaryAmount monetaryAmount;

	@Max(1000L)
	@Min(1L)
	public MonetaryAmount anotherMonetaryAmount;

}
