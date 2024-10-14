/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.getter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ExecutableType;
import jakarta.validation.executable.ValidateOnExecution;

/**
 * @author Hardy Ferentschik
 */
@ValidateOnExecution(type = ExecutableType.GETTER_METHODS)
public class OnlyGetterValidated {
	@NotNull
	public String foo() {
		return null;
	}

	@NotNull
	public String getFoo() {
		return null;
	}
}
