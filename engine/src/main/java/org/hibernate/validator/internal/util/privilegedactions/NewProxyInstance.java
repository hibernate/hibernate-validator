/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.security.PrivilegedAction;

/**
 * Execute proxy creation as privileged action.
 *
 * @author Guillaume Smet
 */
public final class NewProxyInstance<T> implements PrivilegedAction<T> {

	private final ClassLoader classLoader;
	private final Class<?>[] interfaces;
	private final InvocationHandler invocationHandler;

	public static <T> NewProxyInstance<T> action(ClassLoader classLoader, Class<T> interfaze, InvocationHandler invocationHandler) {
		return new NewProxyInstance<T>( classLoader, interfaze, invocationHandler );
	}

	public static NewProxyInstance<Object> action(ClassLoader classLoader, Class<?>[] interfaces, InvocationHandler invocationHandler) {
		return new NewProxyInstance<Object>( classLoader, interfaces, invocationHandler );
	}

	private NewProxyInstance(ClassLoader classLoader, Class<?>[] interfaces, InvocationHandler invocationHandler) {
		this.classLoader = classLoader;
		this.interfaces = interfaces;
		this.invocationHandler = invocationHandler;
	}

	private NewProxyInstance(ClassLoader classLoader, Class<T> interfaze, InvocationHandler invocationHandler) {
		this.classLoader = classLoader;
		this.interfaces = new Class<?>[] { interfaze };
		this.invocationHandler = invocationHandler;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T run() {
		return (T) Proxy.newProxyInstance( classLoader, interfaces, invocationHandler );
	}
}
