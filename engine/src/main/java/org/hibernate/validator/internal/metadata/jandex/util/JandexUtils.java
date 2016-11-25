/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.jandex.util;

import java.util.Map;

import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import org.jboss.jandex.Type;

/**
 * Utility methods used for Jandex metadata retrieval.
 *
 * @author Marko Bekhta
 */
public final class JandexUtils {

	private static final Log log = LoggerFactory.make();

	private JandexUtils() {
	}

	/**
	 * Finds a class ({@link Class}) for a given name.
	 *
	 * @param name a name of a class to find
	 *
	 * @return a found {@link Class}
	 */
	public static Class<?> getClassForName(String name) {
		try {
			return Class.forName( name.toString() );
		}
		catch (ClassNotFoundException e) {
			throw log.getFindingClassReflectionJandexIndexException( name.toString(), e );
		}
	}

	/**
	 * Indicates if the type is considered indexable (ie is an {@link Iterable}, an array or a {@link Map}).
	 *
	 * @param type the type to inspect.
	 *
	 * @return Returns {@code true} if the type is indexable.
	 */
	public static boolean isIndexable(Type type) {
		// TODO: Can this property be somehow determined from a type parameter and without converting it to a class ???
		return ReflectionHelper.isIndexable( getClassForName( type.name().toString() ) );
	}
}
