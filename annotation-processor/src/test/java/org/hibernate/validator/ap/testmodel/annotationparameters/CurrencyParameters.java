/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.annotationparameters;

import javax.money.MonetaryAmount;

import org.hibernate.validator.constraints.Currency;

/**
 * @author Marko Bekhta
 */
public class CurrencyParameters {

	@Currency("USD")
	private String val1;

	@Currency("aad")
	private MonetaryAmount val2;

}
