/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.logging.formatter;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Used with JBoss Logging to display array of class names in log messages.
 *
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class ArrayOfClassesObjectFormatter {

	private final String stringRepresentation;

	public ArrayOfClassesObjectFormatter(Class<?>[] classes) {
		this.stringRepresentation = Arrays.stream( classes )
				.map( c -> c.getName() )
				.collect( Collectors.joining( ", " ) );
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
