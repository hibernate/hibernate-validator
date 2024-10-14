/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

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
