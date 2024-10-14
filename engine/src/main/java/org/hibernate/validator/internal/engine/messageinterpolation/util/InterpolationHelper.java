/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities used for message interpolation.
 *
 * @author Guillaume Smet
 */
public class InterpolationHelper {

	public static final char BEGIN_TERM = '{';
	public static final char END_TERM = '}';
	public static final char EL_DESIGNATOR = '$';
	public static final char ESCAPE_CHARACTER = '\\';

	private static final Pattern ESCAPE_MESSAGE_PARAMETER_PATTERN = Pattern.compile( "([\\" + ESCAPE_CHARACTER + BEGIN_TERM + END_TERM + EL_DESIGNATOR + "])" );

	private InterpolationHelper() {
	}

	public static String escapeMessageParameter(String messageParameter) {
		if ( messageParameter == null ) {
			return null;
		}
		return ESCAPE_MESSAGE_PARAMETER_PATTERN.matcher( messageParameter ).replaceAll( Matcher.quoteReplacement( String.valueOf( ESCAPE_CHARACTER ) ) + "$1" );
	}

}
