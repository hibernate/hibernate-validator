/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.valuehandling;

import java.lang.reflect.Type;

/**
 * Implementations unwrap given elements prior to validation.
 * <p>
 * Unwrapper implementations can be registered when bootstrapping a validator or validator factory. Note that when more
 * than one unwrapper implementation is suitable to unwrap a given element, it is not specified which of the
 * implementations is chosen.
 * <p>
 * Implementations must be thread-safe.
 *
 * @param <T> the type which can be unwrapped by a specific implementation. The value for this type parameter must
 * either resolve to a non-parameterized type (i.e. because the type is not using generics or because the raw
 * type is used instead of the generic version) or all of its own type parameters must be unbounded
 * wildcard types (i.e. &lt;?&gt;).
 *
 * @author Gunnar Morling
 * @hv.experimental This SPI is considered experimental and may change in future revisions
 */
public abstract class ValidatedValueUnwrapper<T> {

	/**
	 * Retrieves the value to be validated from the given wrapper object.
	 *
	 * @param value the wrapper object to retrieve the value from
	 *
	 * @return the unwrapped value to be validated
	 */
	public abstract Object handleValidatedValue(T value);

	/**
	 * Retrieves the declared (static) type of the unwrapped object as to be used for constraint validator resolution.
	 *
	 * @param valueType the declared type of the wrapper object
	 *
	 * @return the declared type of the unwrapped object
	 */
	public abstract Type getValidatedValueType(Type valueType);
}
