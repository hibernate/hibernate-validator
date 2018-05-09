/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.logging.formatter;

import org.hibernate.validator.internal.properties.ConstrainableType;

/**
 * Used with JBoss Logging to display {@link ConstrainableType} names in log messages.
 *
 * @author Gunnar Morling
 * @author Marko Bekhta
 */
public class ConstrainableTypeObjectFormatter {

	private final String stringRepresentation;

	public ConstrainableTypeObjectFormatter(ConstrainableType constrainableType) {
		this.stringRepresentation = constrainableType.getName();
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
