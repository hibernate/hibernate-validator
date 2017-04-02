/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.money;

import javax.money.MonetaryAmount;

import org.hibernate.validator.internal.constraintvalidators.bv.BasePositiveValidator;

/**
 * Check that the number being validated positive.
 *
 * @author Marko Bekhta
 */
public class PositiveValidatorForMonetaryAmount extends BasePositiveValidator<MonetaryAmount> {

	@Override
	protected int compare(MonetaryAmount amount) {
		return amount.signum();
	}
}
