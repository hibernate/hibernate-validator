/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package com.example;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

/**
 *
 */
public class CustomerDecimalMin {
	@NotNull
	@DecimalMin("1.00")
	@DecimalMax("100.00")
	private final int cannotBeZero = 0;
}
