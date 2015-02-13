/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package com.example;

import com.example.constraint.ValidName;

/**
 * @author Gunnar Morling
 */
public class RetailOrder {

	@ValidName(message = "{com.example.RetailOrder.name.message}")
	private final String name = null;
}
