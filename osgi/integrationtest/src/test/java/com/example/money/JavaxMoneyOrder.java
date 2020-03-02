/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package com.example.money;

import javax.money.MonetaryAmount;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Currency;

/**
 * @author Guillaume Smet
 */
public class JavaxMoneyOrder {

	@NotBlank
	private String name;

	@DecimalMin(value = "100")
	@Currency("EUR")
	private MonetaryAmount amount;

	public JavaxMoneyOrder(String name, MonetaryAmount amount) {
		this.name = name;
		this.amount = amount;
	}
}
