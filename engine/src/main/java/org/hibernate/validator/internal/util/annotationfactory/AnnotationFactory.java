/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.util.annotationfactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.validator.internal.util.privilegedactions.ConstructorInstance;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredConstructor;

/**
 * Creates live annotations (actually {@link AnnotationProxy} instances) from {@code AnnotationDescriptor}s.
 *
 * @author Paolo Perrotta
 * @author Davide Marchignoli
 * @author Hardy Ferentschik
 * @see AnnotationProxy
 */
public class AnnotationFactory {

	public static <T extends Annotation> T create(AnnotationDescriptor<T> descriptor) {
		@SuppressWarnings("unchecked")
		Class<T> proxyClass = (Class<T>) Proxy.getProxyClass(
				run( GetClassLoader.fromClass( descriptor.type() ) ),
				descriptor.type()
		);
		InvocationHandler handler = new AnnotationProxy( descriptor );
		try {
			return getProxyInstance( proxyClass, handler );
		}
		catch ( RuntimeException e ) {
			throw e;
		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	private static <T extends Annotation> T getProxyInstance(Class<T> proxyClass, InvocationHandler handler) throws
			SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		final Constructor<T> constructor = run( GetDeclaredConstructor.action(
				proxyClass,
				InvocationHandler.class
		) );
		return run( ConstructorInstance.action( constructor, handler ) );
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
