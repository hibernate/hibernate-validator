/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valuehandling;

/**
 * Determines how unwrapping of values should occur.
 *
 * @author Hardy Ferentschik
 * @see org.hibernate.validator.valuehandling.UnwrapValidatedValue
 * @see org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper
 */
public enum UnwrapMode {
	/**
	 * Automatic occurs if there is no explicit {@code UnwrapValidatedValue} annotation. Whether unwrapping occurs depends
	 * on the number and type of the registered {@code ValidatedValueUnwrapper} instances and the available constraint
	 * validator instances for the wrapper as well as wrapped value.
	 */
	AUTOMATIC,
	/**
	 * Unwrapping of the value is explicitly configured, eg via {@code UnwrapValidatedValue}
	 */
	UNWRAP,
	/**
	 * Unwrapping the value is explicitly prohibited, eg via {@code UnwrapValidatedValue(false)}
	 */
	SKIP_UNWRAP
}
