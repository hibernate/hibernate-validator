/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ExecutableType;
import jakarta.validation.executable.ValidateOnExecution;

/**
 * @author Hardy Ferentschik
 */
public class CIA extends SecretServiceBase {

	// The @ValidateOnExecution annotations here are expected to be ignored
	// since the methods override super-type methods

	@ValidateOnExecution(type = ExecutableType.NONE)
	@Override
	public void whisper(@NotNull String secret) {
	}
}
