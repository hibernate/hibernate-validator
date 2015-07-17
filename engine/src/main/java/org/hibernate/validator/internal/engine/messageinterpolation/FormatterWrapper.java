/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation;

import java.util.Formatter;
import java.util.Locale;

/**
 * A wrapper class for {@code java.util.Formatter#format} avoiding lookup problems in EL engines due to
 * ambiguous method resolution for {@code format}.
 *
 * @author Hardy Ferentschik
 */
public class FormatterWrapper {
	private final Formatter formatter;

	public FormatterWrapper(Locale locale) {
		this.formatter = new Formatter( locale );
	}

	public String format(String format, Object... args) {
		return formatter.format( format, args ).toString();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "FormatterWrapper" );
		sb.append( "{}" );
		return sb.toString();
	}
}


