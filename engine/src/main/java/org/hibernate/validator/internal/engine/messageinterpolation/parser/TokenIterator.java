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


