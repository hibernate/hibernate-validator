/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.actions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Execute proxy creation as privileged action.
 *
 * @author Guillaume Smet
 */
public final class NewProxyInstance {

	private NewProxyInstance() {
	}

	public static <T> T action(ClassLoader classLoader, Class<T> interfaze, InvocationHandler invocationHandler) {
		return action( classLoader, invocationHandler, interfaze );
	}

	public static Object action(ClassLoader classLoader, Class<?>[] interfaces, InvocationHandler invocationHandler) {
		return action( classLoader, invocationHandler, interfaces );
	}

	@SuppressWarnings("unchecked")
	private static <T> T action(ClassLoader classLoader, InvocationHandler invocationHandler, Class<?>... interfaces) {
		return (T) Proxy.newProxyInstance( classLoader, interfaces, invocationHandler );
	}
}
