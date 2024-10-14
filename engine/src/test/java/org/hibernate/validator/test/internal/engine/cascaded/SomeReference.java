/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.cascaded;

public class SomeReference<T> implements Reference<T> {

	private final T value;

	public SomeReference(T value) {
		this.value = value;
	}

	@Override
	public T getValue() {
		return value;
	}
}
