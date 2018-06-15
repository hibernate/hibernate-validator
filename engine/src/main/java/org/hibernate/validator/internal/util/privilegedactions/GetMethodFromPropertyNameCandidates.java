/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.util.Set;

/**
 * Returns the method with the specified property name or {@code null} if it does not exist. This action will
 * iterate through getter name candidates and return the first found method.
 * <p>
 * {@code GetMethodFromPropertyName#lookForMethodsOnSuperClass} parameter controls if we need to check for methods on
 * superclasses/implemented interfaces or not, if equals to {@code false} it will use
 * {@link Class#getDeclaredMethod(String, Class[])}, and {@link Class#getMethod(String, Class[])} otherwise.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Marko Bekhta
 */
public final class GetMethodFromPropertyNameCandidates implements PrivilegedAction<Method> {

	private final Class<?> clazz;
	private final Set<String> getterNameCandidates;
	private final boolean lookForMethodsOnSuperClass;

	private GetMethodFromPropertyNameCandidates(Class<?> clazz, Set<String> getterNameCandidates, boolean lookForMethodsOnSuperClass) {
		this.clazz = clazz;
		this.getterNameCandidates = getterNameCandidates;
		this.lookForMethodsOnSuperClass = lookForMethodsOnSuperClass;
	}

	public static GetMethodFromPropertyNameCandidates action(Class<?> clazz, Set<String> getterNameCandidates) {
		return new GetMethodFromPropertyNameCandidates( clazz, getterNameCandidates, false );
	}

	public static GetMethodFromPropertyNameCandidates action(Class<?> clazz, Set<String> possibleMethodNames, boolean lookForMethodsOnSuperClass) {
		return new GetMethodFromPropertyNameCandidates( clazz, possibleMethodNames, lookForMethodsOnSuperClass );
	}


	@Override
	public Method run() {
		for ( String methodName : getterNameCandidates ) {
			try {
				return clazz.getDeclaredMethod( methodName );
			}
			catch (NoSuchMethodException e) {
				// just ignore the exception
			}
		}

		if ( lookForMethodsOnSuperClass ) {
			for ( String methodName : getterNameCandidates ) {
				try {
					return clazz.getMethod( methodName );
				}
				catch (NoSuchMethodException e) {
					// just ignore the exception
				}
			}
		}

		return null;
	}
}
