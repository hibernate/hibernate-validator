/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.actions;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Execute instance creation as privileged action.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public final class NewInstance {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private NewInstance() {
	}

	public static <T> T action(Class<T> clazz, String message) {
		try {
			return clazz.getConstructor().newInstance();
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
