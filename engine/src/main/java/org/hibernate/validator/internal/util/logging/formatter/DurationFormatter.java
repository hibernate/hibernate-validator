/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.logging.formatter;

import java.time.Duration;

/**
 * Used with JBoss Logging to display class names in log messages.
 *
 * @author Marko Bekhta
 */
public class DurationFormatter {

	private final String stringRepresentation;

	public DurationFormatter(Duration duration) {
		this.stringRepresentation = format( duration );
	}

	public static String format(Duration duration) {
		return duration.toString();
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
