/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ValidateOnExecution;

/**
 * @author Hardy Ferentschik
 */
public class Snafu {
	@ValidateOnExecution(type = { })
	@NotNull
	public String foo() {
		return null;
	}
}
