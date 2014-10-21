/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */


package org.hibernate.validator.internal.util.privilegedactions;

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
public final class ConstructorInstance<T> implements PrivilegedAction<T> {

	private static final Log log = LoggerFactory.make();

	private final Constructor<T> constructor;
	private final Object[] initArgs;

	public static <T> ConstructorInstance<T> action(Constructor<T> constructor, Object... initArgs) {
		return new ConstructorInstance<T>( constructor, initArgs );
	}

	private ConstructorInstance(Constructor<T> constructor, Object... initArgs) {
		this.constructor = constructor;
		this.initArgs = initArgs;
	}

	public T run() {
		try {
			return constructor.newInstance( initArgs );
		}
		catch ( InstantiationException e ) {
			throw log.getUnableToInstantiateException( constructor.getName(), e );
		}
		catch ( IllegalAccessException e ) {
			throw log.getUnableToInstantiateException( constructor.getName(), e );
		}
		catch ( InvocationTargetException e ) {
			throw log.getUnableToInstantiateException( constructor.getName(), e );
		}
		catch ( RuntimeException e ) {
			throw log.getUnableToInstantiateException( constructor.getName(), e );
		}
	}
}
