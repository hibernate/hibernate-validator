/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling.model;

/**
 * @author Gunnar Morling
 */
class UiInput<T> {

	private final T value;

	public UiInput(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}
}
