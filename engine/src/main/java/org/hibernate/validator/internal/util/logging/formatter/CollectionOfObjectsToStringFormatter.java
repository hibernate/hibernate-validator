/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.logging.formatter;

import java.util.Collection;

import org.hibernate.validator.internal.util.StringHelper;

/**
 * Used with JBoss Logging to display collections of objects using toString() in log messages.
 *
 * @author Guillaume Smet
 */
public class CollectionOfObjectsToStringFormatter {

	private final String stringRepresentation;

	public CollectionOfObjectsToStringFormatter(Collection<?> objects) {
		this.stringRepresentation = StringHelper.join( objects, ", " );
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
