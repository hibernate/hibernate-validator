/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter12.propertypath;

import jakarta.validation.constraints.Size;

public class Person {

	@Size(min = 5)
	String name;

	Person(String name) {
		this.name = name;
	}
}
