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
public final class JandexHelper {

	private static final Log log = LoggerFactory.make();

	private JandexHelper() {
	}

	/**
	 * @return an instance of {@link JandexHelper}
	 */
	public static JandexHelper getInstance() {
		return new JandexHelper();
	}

	/**
	 * Finds a class ({@link Class}) for a given name.
	 *
	 * @param name a name of a class to find
	 *
	 * @return a found {@link Class}
	 */
	public Class<?> getClassForName(String name) {
		// TODO: change how class are loaded (from GM: Using this one without passing a classloader may give us trouble due to classes not being visible to
		// the implicitly used loader. You can check out org.hibernate.validator.internal.util.privilegedactions.LoadClass and its usage as a starting point.
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
	public boolean isIndexable(Type type) {
		// TODO: Can this property be somehow determined from a type parameter and without converting it to a class ???
		return ReflectionHelper.isIndexable( getClassForName( type.name().toString() ) );
	}

	/**
	 * Checks if given type is a {@link Map} implementation.
	 *
	 * @param type a type to check
	 *
	 * @return {@code true} if given type is an implementation of a {@link Map}, {@code false} otherwise
	 */
	public boolean isMap(Type type) {
		// TODO: Can this property be somehow determined from a type parameter and without converting it to a class ???
		return ReflectionHelper.isMap( getClassForName( type.name().toString() ) );
	}

	/**
	 * Checks if given type is an {@link Iterable} implementation.
	 *
	 * @param type a type to check
	 *
	 * @return {@code true} if given type is an implementation of an {@link Iterable}, {@code false} otherwise
	 */
	public boolean isIterable(Type type) {
		// TODO: Can this property be somehow determined from a type parameter and without converting it to a class ???
		return ReflectionHelper.isIterable( getClassForName( type.name().toString() ) );
	}
}
