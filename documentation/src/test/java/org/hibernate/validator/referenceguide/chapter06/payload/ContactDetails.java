/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter06.payload;

import jakarta.validation.constraints.NotNull;

//tag::include[]
public class ContactDetails {
	@NotNull(message = "Name is mandatory", payload = Severity.Error.class)
	private String name;

	@NotNull(message = "Phone number not specified, but not mandatory",
			payload = Severity.Info.class)
	private String phoneNumber;

	// ...
}
//end::include[]
