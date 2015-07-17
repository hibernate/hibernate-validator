/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsing a message descriptor results in multiple {@code Token}s. Each token
 * keeps track of its token value and whether the token is a parameter which can
 * be interpolated.
 *
 * Note, tokens are not centred around word delimiters, but rather around message parameter and EL expressions.
 * For example, the message descriptor "must be between {min} and {max}" gets parsed into the tokens 'must be between ',
 * '{min}', ' and ', '{max}', where the min and max tokens are parameters.
 *
 * @author Hardy Ferentschik
 */
public class Token {
	private static final Pattern ESCAPED_OPENING_CURLY_BRACE = Pattern.compile( "\\\\\\{" );
	private static final Pattern ESCAPED_CLOSING_CURLY_BRACE = Pattern.compile( "\\\\\\}" );

	private boolean isParameter;
	private boolean isEL;
	private boolean terminated;
	private String value;

	private StringBuilder builder;

	public Token(String tokenStart) {
		builder = new StringBuilder();
		builder.append( tokenStart );
	}

	public Token(char tokenStart) {
		this( String.valueOf( tokenStart ) );
	}

	public void append(char character) {
		builder.append( character );
	}

	public void makeParameterToken() {
		isParameter = true;
	}

	public void makeELToken() {
		makeParameterToken();
		isEL = true;
	}

	public void terminate() {
		value = builder.toString();
		if ( isEL ) {
			// HSEARCH-834 curly braces need to be un-escaped prior to be passed to the EL engine
			Matcher matcher = ESCAPED_OPENING_CURLY_BRACE.matcher( value );
			value = matcher.replaceAll( "{" );

			matcher = ESCAPED_CLOSING_CURLY_BRACE.matcher( value );
			value = matcher.replaceAll( "}" );
		}
		builder = null;
		terminated = true;
	}

	public boolean isParameter() {
		return isParameter;
	}

	public String getTokenValue() {
		if ( !terminated ) {
			throw new IllegalStateException( "Trying to retrieve token value for unterminated token" );
		}
		return value;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder( "Token{" );
		sb.append( "value='" ).append( value ).append( '\'' );
		sb.append( ", terminated=" ).append( terminated );
		sb.append( ", isEL=" ).append( isEL );
		sb.append( ", isParameter=" ).append( isParameter );
		sb.append( '}' );
		return sb.toString();
	}
}


