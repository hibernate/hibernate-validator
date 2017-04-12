/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.logging.formatter;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Used with JBoss Logging to display collection of class names in log messages.
 *
 * @author Guillaume Smet
 */
public class CollectionOfClassesObjectFormatter {

	private final String stringRepresentation;

	public CollectionOfClassesObjectFormatter(Collection<? extends Class<?>> classes) {
		this.stringRepresentation = classes.stream()
				.map( c -> c.getName() )
				.collect( Collectors.joining( ", " ) );
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
