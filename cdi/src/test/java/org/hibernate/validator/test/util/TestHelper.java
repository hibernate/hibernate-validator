package org.hibernate.validator.test.util;

/**
 * @author Hardy Ferentschik
 */
public class TestHelper {
	private TestHelper() {
	}

	public static String getTestPackagePath(Class<?> clazz) {
		return clazz.getPackage().getName().replace( '.', '/' ).concat( "/" );
	}
}
