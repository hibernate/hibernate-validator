/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.logging.formatter;

/**
 * Used with JBoss Logging to display arrays of objects in log messages.
 *
 * @author Matthias Kurz
 */
public class ObjectFormatter {

	private final String stringRepresentation;

	public ObjectFormatter(Object object) {
		this.stringRepresentation = object.toString();
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
