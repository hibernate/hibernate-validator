/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.logging.formatter;

import java.util.Arrays;

/**
 * Used with JBoss Logging to display arrays of objects in log messages.
 *
 * @author Guillaume Smet
 */
public class ObjectArrayFormatter {

	private final String stringRepresentation;

	public ObjectArrayFormatter(Object[] array) {
		this.stringRepresentation = Arrays.toString( array );
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
