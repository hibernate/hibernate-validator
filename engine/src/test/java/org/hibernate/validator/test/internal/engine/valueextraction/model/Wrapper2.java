/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

/**
 * @author Guillaume Smet
 */
public class Wrapper2<T> implements IWrapper21<T>, IWrapper22<T> {

	private T property;

	public Wrapper2(T property) {
		this.property = property;
	}

	@Override
	public T getProperty() {
		return property;
	}
}
