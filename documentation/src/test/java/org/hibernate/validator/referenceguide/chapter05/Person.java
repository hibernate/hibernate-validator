/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter05;

//end::include[]
import jakarta.validation.constraints.NotNull;

//tag::include[]
public class Person {

	@NotNull
	private String name;

	public Person(String name) {
		this.name = name;
	}

	// getters and setters ...
}
//end::include[]
