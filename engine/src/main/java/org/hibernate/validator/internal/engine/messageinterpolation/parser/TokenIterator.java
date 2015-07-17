/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows to iterate over a list of message tokens and replace parameters.
 *
 * @author Hardy Ferentschik
 */
public class TokenIterator {
	private final List<Token> tokenList;

	private int currentPosition;
	private Token currentToken;
	private boolean allInterpolationTermsProcessed;
	private boolean currentTokenAvailable;

	public TokenIterator(List<Token> tokens) {
		this.tokenList = new ArrayList<Token>( tokens );
	}

	/**
	 * Called to advance the next interpolation term of the message descriptor. This message can be called multiple times.
	 * Once it returns {@code false} all interpolation terms have been processed and {@link #getInterpolatedMessage()}
	 * can be called.
	 *
	 * @return Returns {@code true} in case there are more message parameters, {@code false} otherwise.
	 *
	 * @throws MessageDescriptorFormatException in case the message descriptor is invalid
	 */
	public boolean hasMoreInterpolationTerms() throws MessageDescriptorFormatException {
		while ( currentPosition < tokenList.size() ) {
			currentToken = tokenList.get( currentPosition );
			currentPosition++;
			if ( currentToken.isParameter() ) {
				currentTokenAvailable = true;
				return true;
			}
		}
		allInterpolationTermsProcessed = true;
		return false;
	}

	/**
	 * @return Returns the next interpolation term
	 */
	public String nextInterpolationTerm() {
		if ( !currentTokenAvailable ) {
			throw new IllegalStateException(
					"Trying to call #nextInterpolationTerm without calling #hasMoreInterpolationTerms"
			);
		}
		currentTokenAvailable = false;
		return currentToken.getTokenValue();
	}

	/**
	 * Replaces the current interpolation term with the given string.
	 *
	 * @param replacement The string to replace the current term with.
	 */
	public void replaceCurrentInterpolationTerm(String replacement) {
		Token token = new Token( replacement );
		token.terminate();
		tokenList.set( currentPosition - 1, token );
	}

	public String getInterpolatedMessage() {
		if ( !allInterpolationTermsProcessed ) {
			throw new IllegalStateException( "Not all interpolation terms have been processed yet." );
		}
		StringBuilder messageBuilder = new StringBuilder();
		for ( Token token : tokenList ) {
			messageBuilder.append( token.getTokenValue() );
		}

		return messageBuilder.toString();
	}
}


