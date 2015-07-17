/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.testutil;

import java.io.InputStream;

/**
 * Helps tests with using a specific file as {@code META-INF/validation.xml}.
 *
 * @author Gunnar Morling
 */
public class ValidationXmlTestHelper {

	private final Class<?> clazz;

	/**
	 * Creates a new {@code ValidationXmlTestHelper}.
	 *
	 * @param clazz
	 *            A class through which the specified {@code validation.xml} stand-in will be loaded.
	 */
	public ValidationXmlTestHelper(Class<?> clazz) {
		this.clazz = clazz;
	}

	/**
	 * Executes the given runnable, using the specified file as replacement for
	 * {@code META-INF/validation.xml}.
	 *
	 * @param validationXmlName The file to be used as validation.xml file.
	 * @param runnable The runnable to execute.
	 */
	public void runWithCustomValidationXml(final String validationXmlName, Runnable runnable) {
		ClassLoader previousContextCl = Thread.currentThread().getContextClassLoader();

		try {
			Thread.currentThread().setContextClassLoader(
					new ClassLoader( previousContextCl ) {
						@Override
						public InputStream getResourceAsStream(String name) {
							if ( name.equals( "META-INF/validation.xml" ) ) {
								return clazz.getResourceAsStream( validationXmlName );
							}

							return super.getResourceAsStream( name );
						}
					}
			);
			runnable.run();
		}
		finally {
			Thread.currentThread().setContextClassLoader( previousContextCl );
		}
	}
}
