/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

/**
 * @author Guillaume Smet
 */
public class Wrapper1<T> implements IWrapper11<T> {

	private T property;

	public Wrapper1(T property) {
		this.property = property;
	}

	@Override
	public T getProperty() {
		return property;
	}
}
