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
public class Property<T> {

	private final T value;

	public Property(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}
}
