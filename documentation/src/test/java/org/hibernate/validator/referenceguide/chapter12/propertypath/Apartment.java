/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter12.propertypath;

import jakarta.validation.Valid;

public class Apartment {

	@Valid
	Person resident;

	Apartment(Person resident) {
		this.resident = resident;
	}
}
