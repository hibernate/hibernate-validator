/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.constraintvalidation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @author Gunnar Morling
 */
@SuppressWarnings("unused")
public class User {

	private String firstName;

	@NotNull
	private String lastName;

	private String middleName;

	@Size(min = 5, max = 50)
	private String address1;

	private String address2;
}
