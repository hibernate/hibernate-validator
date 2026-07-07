/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.bean;

final class SimpleBeanHolder<T> implements BeanHolder<T> {

	private final T instance;

	SimpleBeanHolder(T instance) {
		this.instance = instance;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ "instance=" + instance
				+ "]";
	}

	@Override
	public T get() {
		return instance;
	}

	@Override
	public void close() {
		// No-op
	}
}
