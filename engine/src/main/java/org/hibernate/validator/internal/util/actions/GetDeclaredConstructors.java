package org.hibernate.validator.internal.util.actions;

import java.lang.reflect.Constructor;

/**
 * Returns the declared constructors of the specified class.
 *
 * @author Gunnar Morling
 */
public final class GetDeclaredConstructors {

	private GetDeclaredConstructors() {
	}

	public static Constructor<?>[] action(Class<?> clazz) {
		return clazz.getDeclaredConstructors();
	}
}
