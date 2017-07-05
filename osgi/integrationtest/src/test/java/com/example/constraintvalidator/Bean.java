/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package com.example.constraintvalidator;

public class Bean {

	@MustMatch("Foo")
	public String getFoo() {
		return "Bar";
	}
}
