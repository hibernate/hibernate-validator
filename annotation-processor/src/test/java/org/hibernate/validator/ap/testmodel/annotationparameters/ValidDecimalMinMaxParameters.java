/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.annotationparameters;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

/**
 * @author Marko Bekhta
 */
public class ValidDecimalMinMaxParameters {

	@DecimalMax(value = "0")
	@DecimalMin(value = "0")
	private BigDecimal decimal1;

	@DecimalMax(value = "0.00")
	@DecimalMin(value = "0.00")
	private BigDecimal decimal2;

	@DecimalMax(value = "123")
	@DecimalMin(value = "123")
	private BigDecimal decimal3;

	@DecimalMax(value = "-123")
	@DecimalMin(value = "-123")
	private BigDecimal decimal4;

	@DecimalMax(value = "1.23E3")
	@DecimalMin(value = "1.23E3")
	private BigDecimal decimal5;

	@DecimalMax(value = "1.23E+3")
	@DecimalMin(value = "1.23E+3")
	private BigDecimal decimal6;

	@DecimalMax(value = "12.3E+7")
	@DecimalMin(value = "12.3E+7")
	private BigDecimal decimal7;

	@DecimalMax(value = "12.0")
	@DecimalMin(value = "12.0")
	private BigDecimal decimal8;

	@DecimalMax(value = "12.3")
	@DecimalMin(value = "12.3")
	private BigDecimal decimal9;

	@DecimalMax(value = "0.00123")
	@DecimalMin(value = "0.00123")
	private BigDecimal decimal10;

	@DecimalMax(value = "-1.23E-12")
	@DecimalMin(value = "-1.23E-12")
	private BigDecimal decimal11;

	@DecimalMax(value = "1234.5E-4")
	@DecimalMin(value = "1234.5E-4")
	private BigDecimal decimal12;

	@DecimalMax(value = "0E+7")
	@DecimalMin(value = "0E+7")
	private BigDecimal decimal13;

	@DecimalMax(value = "-0")
	@DecimalMin(value = "-0")
	private BigDecimal decimal14;

	@DecimalMax.List({ @DecimalMax(value = "-0"), @DecimalMax(value = "0E+7") })
	@DecimalMin.List({ @DecimalMin(value = "-0"), @DecimalMin(value = "0E+7") })
	private BigDecimal decimal15;

	public ValidDecimalMinMaxParameters(
			@DecimalMax(value = "-0") BigDecimal decimal1,
			@DecimalMax(value = "0E+7") BigDecimal decimal2,
			@DecimalMax(value = "1234.5E-4") BigDecimal decimal3
	) {

	}

	public void doSomething(
			@DecimalMax(value = "-0") BigDecimal decimal1,
			@DecimalMax(value = "0E+7") BigDecimal decimal2,
			@DecimalMax(value = "1234.5E-4") BigDecimal decimal3
	) {

	}

	@DecimalMax(value = "1234.5E-4")
	public BigDecimal doSomething() {
		return BigDecimal.ONE;
	}

}
