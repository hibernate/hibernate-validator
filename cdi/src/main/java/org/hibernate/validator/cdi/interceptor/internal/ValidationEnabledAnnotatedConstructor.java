/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cdi.interceptor.internal;

import java.lang.reflect.Constructor;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;

/**
 * @author Hardy Ferentschik
 */
public class ValidationEnabledAnnotatedConstructor<T> extends ValidationEnabledAnnotatedCallable<T>
		implements AnnotatedConstructor<T> {
	public ValidationEnabledAnnotatedConstructor(AnnotatedConstructor<T> constructor) {
		super( constructor );
	}

	@Override
	@SuppressWarnings("unchecked")
	public Constructor<T> getJavaMember() {
		return (Constructor<T>) getWrappedCallable().getJavaMember();
	}
}
