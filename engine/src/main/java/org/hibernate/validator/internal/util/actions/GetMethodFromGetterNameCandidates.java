/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.actions;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Returns the method with the specified property name or {@code null} if it does not exist. This action will
 * iterate through getter name candidates and return the first found method.
 * <p>
 * {@code GetMethodFromPropertyName#lookForMethodsInHierarchy} parameter controls if we need to check for methods on
 * superclasses/implemented interfaces or not, if equals to {@code false} it will use
 * {@link Class#getDeclaredMethod(String, Class[])}, and {@link Class#getMethod(String, Class[])} otherwise.
 *
 * @author Marko Bekhta
 */
public final class GetMethodFromGetterNameCandidates {

	private GetMethodFromGetterNameCandidates() {
	}

	public static Method action(Class<?> clazz, List<String> getterNameCandidates) {
		return action( clazz, getterNameCandidates, false );
	}

	public static Method action(Class<?> clazz, List<String> possibleMethodNames, boolean lookForMethodsInHierarchy) {
		for ( String methodName : possibleMethodNames ) {
			try {
				if ( lookForMethodsInHierarchy ) {
					return clazz.getMethod( methodName );
				}
				else {
					return clazz.getDeclaredMethod( methodName );
				}
			}
			catch (NoSuchMethodException e) {
				// just ignore the exception
			}
		}

		return null;
	}
}
