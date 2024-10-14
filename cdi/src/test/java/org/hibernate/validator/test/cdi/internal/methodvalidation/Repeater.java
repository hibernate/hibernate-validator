/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ExecutableType;
import jakarta.validation.executable.ValidateOnExecution;

/**
 * @author Hardy Ferentschik
 */
@ValidateOnExecution(type = { ExecutableType.NON_GETTER_METHODS, ExecutableType.GETTER_METHODS })
public interface Repeater<T> {
	String repeat(@NotNull String in);

	@NotNull
	T reverse(T in);

	@NotNull
	String getHelloWorld();
}
