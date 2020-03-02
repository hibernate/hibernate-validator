/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
