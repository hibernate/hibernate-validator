/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cdi.interceptor.internal;

import java.lang.reflect.Method;

import jakarta.enterprise.inject.spi.AnnotatedMethod;

/**
 * @author Hardy Ferentschik
 */
public class ValidationEnabledAnnotatedMethod<T> extends ValidationEnabledAnnotatedCallable<T>
		implements AnnotatedMethod<T> {

	public ValidationEnabledAnnotatedMethod(AnnotatedMethod<T> method) {
		super( method );
	}

	@Override
	public Method getJavaMember() {
		return (Method) getWrappedCallable().getJavaMember();
	}
}
