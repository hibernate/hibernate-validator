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
 * Returns the method with the specified property name or {@code null} if it does not exist. This method will prepend
 * 'is' and 'get' to the property name and capitalize the first letter.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public final class GetMethodFromPropertyName implements PrivilegedAction<Method> {
	private final Class<?> clazz;
	private final String property;

	public static GetMethodFromPropertyName action(Class<?> clazz, String property) {
		return new GetMethodFromPropertyName( clazz, property );
	}

	private GetMethodFromPropertyName(Class<?> clazz, String property) {
		this.clazz = clazz;
		this.property = property;
	}

	@Override
	public Method run() {
		try {
			char[] string = property.toCharArray();
			string[0] = Character.toUpperCase( string[0] );
			String fullMethodName = new String( string );
			try {
				return clazz.getMethod( "get" + fullMethodName );
			}
			catch ( NoSuchMethodException e ) {
				return clazz.getMethod( "is" + fullMethodName );
			}
		}
		catch ( NoSuchMethodException e ) {
			return null;
		}
	}
}
