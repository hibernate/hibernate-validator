/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package com.example;

import javax.validation.constraints.Min;

/**
 * @author Gunnar Morling
 */
public class Customer {

	@Min(1)
	private final int status = 0;
}
