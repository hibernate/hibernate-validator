/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.logging.formatter;

/**
 * Used with JBoss Logging to display class names in log messages.
 *
 * @author Gunnar Morling
 */
public class ClassObjectFormatter {

	private final String stringRepresentation;

	public ClassObjectFormatter(Class<?> clazz) {
		this.stringRepresentation = clazz.getName();
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
