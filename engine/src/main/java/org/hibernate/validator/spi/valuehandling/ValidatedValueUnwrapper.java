/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * wildcard types (i.e. <?>).
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
