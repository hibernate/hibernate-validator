/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.actions;

import java.lang.reflect.Method;

/**
 * Returns the declared method with the specified name and parameter types or {@code null} if it does not exist.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public final class GetDeclaredMethod {

	private GetDeclaredMethod() {
	}

	public static Method action(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		return action( clazz, methodName, false, parameterTypes );
	}

	/**
	 * Before using this method, you need to check the {@code HibernateValidatorPermission.ACCESS_PRIVATE_MEMBERS}
	 * permission against the security manager.
	 */
	public static Method andMakeAccessible(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		return action( clazz, methodName, true, parameterTypes );
	}


	private static Method action(Class<?> clazz, String methodName, boolean makeAccessible, Class<?>[] parameterTypes) {
		try {
			Method method = clazz.getDeclaredMethod( methodName, parameterTypes );

			if ( makeAccessible ) {
				method.setAccessible( true );
			}

			return method;
		}
		catch (NoSuchMethodException e) {
			return null;
		}
	}
}
