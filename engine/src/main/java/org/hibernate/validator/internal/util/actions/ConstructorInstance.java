/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */


package org.hibernate.validator.internal.util.actions;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Execute instance creation as privileged action.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public final class ConstructorInstance {
	private ConstructorInstance() {
	}

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	public static <T> T action(Constructor<T> constructor, Object... initArgs) {
		try {
			return constructor.newInstance( initArgs );
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw LOG.getUnableToInstantiateException( constructor.getDeclaringClass(), e );
		}
	}
}
