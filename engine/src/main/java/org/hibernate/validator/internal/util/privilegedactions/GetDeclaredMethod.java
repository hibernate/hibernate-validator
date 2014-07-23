/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.reflect.Method;
import java.security.PrivilegedAction;

/**
 * Returns the declared method with the specified name and parameter types or {@code null} if it does not exist.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public final class GetDeclaredMethod implements PrivilegedAction<Method> {
	private final Class<?> clazz;
	private final String methodName;
	private final Class<?>[] parameterTypes;

	public static GetDeclaredMethod action(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		return new GetDeclaredMethod( clazz, methodName, parameterTypes );
	}

	private GetDeclaredMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		this.clazz = clazz;
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
	}

	@Override
	public Method run() {
		try {
			return clazz.getDeclaredMethod( methodName, parameterTypes );
		}
		catch ( NoSuchMethodException e ) {
			return null;
		}
	}
}
