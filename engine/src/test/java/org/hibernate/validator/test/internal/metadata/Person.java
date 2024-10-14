/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.metadata;

import jakarta.validation.constraints.NotEmpty;

/**
 * @author Hardy Ferentschik
 */
public interface Person {
	@NotEmpty(groups = PersonValidation.class)
	String getFirstName();

	String getMiddleName();

	@NotEmpty
	String getLastName();

	interface PersonValidation {
	}
}
