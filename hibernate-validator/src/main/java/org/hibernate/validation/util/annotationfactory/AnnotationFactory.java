// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.util.annotationfactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;


/**
 * Creates live annotations (actually <code>AnnotationProxies</code>) from <code>AnnotationDescriptors</code>.
 *
 * @author Paolo Perrotta
 * @author Davide Marchignoli
 * @see AnnotationProxy
 */
public class AnnotationFactory {

	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T create(AnnotationDescriptor descriptor) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        //TODO round 34ms to generate the proxy, hug! is Javassist Faster?
        Class<T> proxyClass = (Class<T>) Proxy.getProxyClass( classLoader, descriptor.type() );
		InvocationHandler handler = new AnnotationProxy( descriptor );
		try {
			return getProxyInstance( proxyClass, handler );
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException( e );
		}
	}

	private static <T extends Annotation> T getProxyInstance(Class<T> proxyClass, InvocationHandler handler) throws
			SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Constructor<T> constructor = proxyClass.getConstructor( new Class[]{InvocationHandler.class} );
		return constructor.newInstance( new Object[]{handler} );
	}
}
