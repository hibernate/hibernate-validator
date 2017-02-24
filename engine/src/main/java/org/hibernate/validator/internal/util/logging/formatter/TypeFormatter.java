/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.logging.formatter;

import java.lang.reflect.Type;

/**
 * Used with JBoss Logging to display {@link Type} names in log messages.
 *
 * @author Gunnar Morling
 */
public class TypeFormatter {

	private final String stringRepresentation;

	public TypeFormatter(Type type) {
		this.stringRepresentation = type.getTypeName();
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
