/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.reflect.Method;
import java.security.PrivilegedAction;

import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * Returns the method with the specified property name or {@code null} if it is not declared in the specified class.
 * This method will prepend 'is' and 'get' to the property name and capitalize the first letter.
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
		char[] string = property.toCharArray();
		string[0] = Character.toUpperCase( string[0] );
		String fullMethodName = new String( string );

		for ( String prefix : ReflectionHelper.PROPERTY_ACCESSOR_PREFIXES ) {
			try {
				return clazz.getDeclaredMethod( prefix + fullMethodName );
			}
			catch (NoSuchMethodException e) {
				// silenlty ignore the exception. Will return null in the end if nothing is found
			}
		}
		return null;
	}
}
