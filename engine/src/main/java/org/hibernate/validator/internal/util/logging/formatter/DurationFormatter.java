/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.logging.formatter;

import java.time.Duration;

/**
 * Used with JBoss Logging to display durations in log messages.
 *
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public class DurationFormatter {

	private final String stringRepresentation;

	public DurationFormatter(Duration duration) {
		if ( Duration.ZERO.equals( duration ) ) {
			this.stringRepresentation = "0";
		}
		else {
			long seconds = duration.getSeconds();
			long days = seconds / ( 24 * 3_600 );
			long hours = seconds / 3_600 % 24;
			long minutes = seconds / 60 % 60;
			int millis = duration.getNano() / 1_000_000;
			int nanos = duration.getNano() % 1_000_000;

			StringBuilder formattedDuration = new StringBuilder();
			appendTimeUnit( formattedDuration, days, "days", "day" );
			appendTimeUnit( formattedDuration, hours, "hours", "hour" );
			appendTimeUnit( formattedDuration, minutes, "minutes", "minute" );
			appendTimeUnit( formattedDuration, seconds % 60, "seconds", "second" );
			appendTimeUnit( formattedDuration, millis, "milliseconds", "millisecond" );
			appendTimeUnit( formattedDuration, nanos, "nanoseconds", "nanosecond" );

			this.stringRepresentation = formattedDuration.toString();
		}
	}

	private void appendTimeUnit(StringBuilder sb, long number, String pluralLabel, String singularLabel) {
		if ( number == 0 ) {
			return;
		}
		if ( sb.length() > 0 ) {
			sb.append( " " );
		}
		sb.append( number )
				.append( " " )
				.append( number == 1 ? singularLabel : pluralLabel );
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
