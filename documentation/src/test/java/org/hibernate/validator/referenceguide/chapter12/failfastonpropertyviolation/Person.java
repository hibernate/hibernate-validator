/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
// tag::include[]
package org.hibernate.validator.referenceguide.chapter12.failfastonpropertyviolation;

//end::include[]
import jakarta.validation.constraints.NotNull;

//tag::include[]
public class Person {

	@NotNull
	private String firstName;

	@NotNull
	private String lastName;

	public Person(@NotNull final String firstName, @NotNull final String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

	//getters, setters, equals and hashcode...
}
//end::include[]
