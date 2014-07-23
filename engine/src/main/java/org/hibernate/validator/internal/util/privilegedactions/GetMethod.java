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
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.reflect.Method;
import java.security.PrivilegedAction;

/**
 * Returns the method with the specified name or {@code null} if it does not exist.
 *
 * @author Emmanuel Bernard
 */
public final class GetMethod implements PrivilegedAction<Method> {
	private final Class<?> clazz;
	private final String methodName;

	public static GetMethod action(Class<?> clazz, String methodName) {
		return new GetMethod( clazz, methodName );
	}

	private GetMethod(Class<?> clazz, String methodName) {
		this.clazz = clazz;
		this.methodName = methodName;
	}

	@Override
	public Method run() {
		try {
			return clazz.getMethod(methodName);
		}
		catch ( NoSuchMethodException e ) {
			return null;
		}
	}
}
