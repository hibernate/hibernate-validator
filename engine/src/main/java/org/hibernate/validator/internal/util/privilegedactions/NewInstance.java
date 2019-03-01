/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedAction;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Execute instance creation as privileged action.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public final class NewInstance<T> implements PrivilegedAction<T> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Class<T> clazz;
	private final String message;
	private final boolean makeAccessible;

	public static <T> NewInstance<T> action(Class<T> clazz, String message) {
		return action( clazz, message, false );
	}

	public static <T> NewInstance<T> action(Class<T> clazz, String message, boolean makeAccessible) {
		return new NewInstance<T>( clazz, message, makeAccessible );
	}

	private NewInstance(Class<T> clazz, String message, boolean makeAccessible) {
		this.clazz = clazz;
		this.message = message;
		this.makeAccessible = makeAccessible;
	}

	@Override
	public T run() {
		try {
			Constructor<T> constructor;
			if ( makeAccessible ) {
				constructor = clazz.getDeclaredConstructor();
				constructor.setAccessible( true );
			}
			else {
				constructor = clazz.getConstructor();
			}
			return constructor.newInstance();
		}
		catch (InstantiationException | NoSuchMethodException | InvocationTargetException e) {
			throw LOG.getUnableToInstantiateException( message, clazz, e );
		}
		catch (IllegalAccessException e) {
			throw LOG.getUnableToInstantiateException( clazz, e );
		}
		catch (RuntimeException e) {
			throw LOG.getUnableToInstantiateException( clazz, e );
		}
	}
}
