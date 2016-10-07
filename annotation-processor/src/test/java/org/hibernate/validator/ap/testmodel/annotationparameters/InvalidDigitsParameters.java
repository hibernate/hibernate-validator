/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.annotationparameters;

import java.math.BigDecimal;
import javax.validation.constraints.Digits;

/**
 * @author Marko Bekhta
 */
public class InvalidDigitsParameters {

	@Digits(integer = -3, fraction = 3)
	private BigDecimal decimal1;

	@Digits(integer = 5, fraction = -3)
	private BigDecimal decimal2;

	@Digits(integer = -5, fraction = -3)
	private BigDecimal decimal3;

	@Digits.List({ @Digits(integer = -5, fraction = -3), @Digits(integer = 5, fraction = -3) })
	private BigDecimal decimal4;

	public InvalidDigitsParameters(
			@Digits(integer = -1, fraction = 3) BigDecimal decimal1,
			@Digits(integer = -5, fraction = -3) BigDecimal decimal2,
			@Digits(integer = 3, fraction = -3) BigDecimal decimal3
	) {

	}

	public void doSomething(
			@Digits(integer = -1, fraction = 3) BigDecimal decimal1,
			@Digits(integer = -5, fraction = -3) BigDecimal decimal2,
			@Digits(integer = 3, fraction = -3) BigDecimal decimal3
	) {

	}

	@Digits(integer = -1, fraction = 3)
	public BigDecimal doSomething() {
		return BigDecimal.ONE;
	}
}
