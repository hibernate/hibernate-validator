/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.actions;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Returns the declared method with the specified name and parameter types in the form of a {@link MethodHandle} or
 * {@code null} if it does not exist or cannot be accessed.
 *
 * @author Guillaume Smet
 */
public final class GetDeclaredMethodHandle {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private GetDeclaredMethodHandle() {
	}

	public static MethodHandle action(Lookup lookup, Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		return action( lookup, clazz, methodName, false, parameterTypes );
	}

	/**
	 * Before using this method on arbitrary classes, you need to check the {@code HibernateValidatorPermission.ACCESS_PRIVATE_MEMBERS}
	 * permission against the security manager, if the calling class exposes the handle to clients.
	 */
	public static MethodHandle andMakeAccessible(Lookup lookup, Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		return action( lookup, clazz, methodName, true, parameterTypes );
	}


	private static MethodHandle action(Lookup lookup, Class<?> clazz, String methodName, boolean makeAccessible, Class<?>... parameterTypes) {
		try {
			Method method = clazz.getDeclaredMethod( methodName, parameterTypes );
			if ( makeAccessible ) {
				method.setAccessible( true );
			}
			return lookup.unreflect( method );
		}
		catch (NoSuchMethodException e) {
			return null;
		}
		catch (IllegalAccessException e) {
			throw LOG.getUnableToAccessMethodException( lookup, clazz, methodName, parameterTypes, e );
		}
	}
}
