/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation.parser;

import java.util.List;

import org.hibernate.validator.internal.util.stereotypes.Lazy;

/**
 * Allows to iterate over a list of message tokens and replace parameters.
 *
 * @author Hardy Ferentschik
 */
public class TokenIterator {
	private final List<Token> tokenList;
	private final String originalMessage;
	@Lazy
	private StringBuilder messageBuilder;

	private int currentPosition;
	private Token currentToken;
	private boolean allInterpolationTermsProcessed;
	private boolean currentTokenAvailable;

	public TokenIterator(String originalMessage, List<Token> tokens) {
		this.tokenList = tokens;
		this.originalMessage = originalMessage;
	}

	/**
	 * Called to advance the next interpolation term of the message descriptor. This message can be called multiple times.
	 * Once it returns {@code false} all interpolation terms have been processed and {@link #getInterpolatedMessage()}
	 * can be called.
	 *
	 * @return Returns {@code true} in case there are more message parameters, {@code false} otherwise.
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
			if ( messageBuilder != null ) {
				messageBuilder.append( currentToken.getTokenValue() );
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
		if ( !currentToken.getTokenValue().equals( replacement ) ) {
			if ( messageBuilder == null ) {
				messageBuilder = new StringBuilder();
				for ( int i = 0; i < currentPosition - 1; i++ ) {
					messageBuilder.append( tokenList.get( i ).getTokenValue() );
				}
			}
		}
		if ( messageBuilder != null ) {
			messageBuilder.append( replacement );
		}
	}

	public String getInterpolatedMessage() {
		if ( !allInterpolationTermsProcessed ) {
			throw new IllegalStateException( "Not all interpolation terms have been processed yet." );
		}

		return messageBuilder != null ? messageBuilder.toString() : originalMessage;
	}
}
