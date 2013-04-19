/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.internal.engine.messageinterpolation;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.MessageInterpolator;

/**
 * Represents a single EL or parameter expression extracted from a message.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public abstract class InterpolationTerm {

	/**
	 * Meta character to designate an EL expression.
	 */
	private static final String EL_DESIGNATION_CHARACTER = "$";

	/**
	 * Regular expression used to split message parameter/expression into expression and leading back slashes
	 */
	private static final Pattern LEADING_ESCAPE_CHARACTER_PATTERN = Pattern.compile( "(\\\\*)(.*)" );

	/**
	 * The actual expression (parameter or EL expression).
	 */
	protected final String expression;

	/**
	 * Leading escape characters for the expression. Needed to determine whether evaluation is needed.
	 */
	protected final String leadingEscapeCharacters;

	/**
	 * The locale for which to interpolate the expression.
	 */
	protected final Locale locale;

	/**
	 * Returns a new interpolation term. Depending on whether the given expression starts with {@link #EL_DESIGNATION_CHARACTER}
	 * , an {@link ElInterpolationTerm} or a {@link ParameterInterpolationTerm} will be returned.
	 *
	 * @param expression the expression for which to create an interpolation term
	 * @param locale the locale of the term
	 *
	 * @return an interpolation term representing the given expression
	 */
	public static InterpolationTerm getInstance(String expression, Locale locale) {
		if ( expression.startsWith( EL_DESIGNATION_CHARACTER ) ) {
			return new ElInterpolationTerm( expression, locale );
		}
		else {
			return new ParameterInterpolationTerm( expression, locale );
		}
	}

	protected InterpolationTerm(String expression, Locale locale) {
		Matcher matcher = LEADING_ESCAPE_CHARACTER_PATTERN.matcher( expression );
		matcher.find();
		this.locale = locale;
		this.leadingEscapeCharacters = matcher.group( 1 );
		this.expression = matcher.group( 2 );
	}

	public boolean needsEvaluation() {
		return leadingEscapeCharacters.length() % 2 == 0;
	}

	/**
	 * Interpolates this term using the given context.
	 *
	 * @param context the interpolator context to be used
	 *
	 * @return a String representing the interpolation result of this term
	 */
	public abstract String interpolate(MessageInterpolator.Context context);

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "InterpolationExpression" );
		sb.append( "{expression='" ).append( expression ).append( '\'' );
		sb.append( ", leadingEscapeCharacters='" ).append( leadingEscapeCharacters ).append( '\'' );
		sb.append( '}' );
		return sb.toString();
	}
}
