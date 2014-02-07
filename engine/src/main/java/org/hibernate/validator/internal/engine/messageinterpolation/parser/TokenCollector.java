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

import java.util.Collections;
import java.util.List;

import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTermType;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * Used to creates a list of tokens from a message descriptor.
 *
 * @author Hardy Ferentschik
 * @see Token
 */
public class TokenCollector {
	public static final char BEGIN_TERM = '{';
	public static final char END_TERM = '}';
	public static final char EL_DESIGNATOR = '$';
	public static final char ESCAPE_CHARACTER = '\\';

	private final String originalMessageDescriptor;
	private final InterpolationTermType interpolationTermType;

	private List<Token> tokenList;
	private ParserState currentParserState;
	private int currentPosition;
	private Token currentToken;

	public TokenCollector(String originalMessageDescriptor, InterpolationTermType interpolationTermType)
			throws MessageDescriptorFormatException {
		this.originalMessageDescriptor = originalMessageDescriptor;
		this.interpolationTermType = interpolationTermType;
		this.currentParserState = new BeginState();
		this.tokenList = newArrayList();

		parse();
	}

	public void terminateToken() {
		if ( currentToken == null ) {
			return;
		}
		currentToken.terminate();
		tokenList.add( currentToken );
		currentToken = null;
	}

	public void appendToToken(char character) {
		if ( currentToken == null ) {
			currentToken = new Token( character );
		}
		else {
			currentToken.append( character );
		}
	}

	public void makeParameterToken() {
		currentToken.makeParameterToken();
	}

	public void makeELToken() {
		currentToken.makeELToken();
	}

	public void next() throws MessageDescriptorFormatException {
		if ( currentPosition == originalMessageDescriptor.length() ) {
			// give the current context the chance to complete
			currentParserState.terminate( this );
			return;
		}
		char currentCharacter = originalMessageDescriptor.charAt( currentPosition );
		currentPosition++;
		switch ( currentCharacter ) {
			case BEGIN_TERM: {
				currentParserState.handleBeginTerm( currentCharacter, this );
				break;
			}
			case END_TERM: {
				currentParserState.handleEndTerm( currentCharacter, this );
				break;
			}
			case EL_DESIGNATOR: {
				currentParserState.handleELDesignator( currentCharacter, this );
				break;
			}
			case ESCAPE_CHARACTER: {
				currentParserState.handleEscapeCharacter( currentCharacter, this );
				break;
			}
			default: {
				currentParserState.handleNonMetaCharacter( currentCharacter, this );
			}
		}
		// make sure the last token is terminated
		terminateToken();
	}

	public void parse() throws MessageDescriptorFormatException {
		currentParserState.start( this );
	}

	public void transitionState(ParserState newState) {
		currentParserState = newState;
	}

	public InterpolationTermType getInterpolationType() {
		return interpolationTermType;
	}

	public List<Token> getTokenList() {
		return Collections.unmodifiableList( tokenList );
	}

	public String getOriginalMessageDescriptor() {
		return originalMessageDescriptor;
	}
}

