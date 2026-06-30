/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;


import java.util.Locale;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.DateTimeFormat;

/**
 * An {@link org.hibernate.validator.constraints.DateTimeFormat} constraint definition.
 * @author Sean Okafor
 * @since 9.2
 */
public class DateTimeFormatDef extends ConstraintDef<DateTimeFormatDef, DateTimeFormat> {

	public DateTimeFormatDef() {
		super( DateTimeFormat.class );
	}

	public DateTimeFormatDef pattern(String pattern) {
		addParameter( "pattern", pattern );
		return this;
	}

	public DateTimeFormatDef locale(String... locale) {
		addParameter( "locale", locale );
		return this;
	}

	public DateTimeFormatDef locale(Locale... locale) {
		final String[] locales = new String[locale.length];
		for ( int i = 0; i < locales.length; i++ ) {
			locales[i] = locale[i].toLanguageTag();
		}
		addParameter( "locale", locales );
		return this;
	}

	public DateTimeFormatDef lenient(boolean lenient) {
		addParameter( "lenient", lenient );
		return this;
	}

}
