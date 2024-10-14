/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
