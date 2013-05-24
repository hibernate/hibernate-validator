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

/**
 * @author Hardy Ferentschik
 */
public class MessageDescriptorParser {

	private static final char BEGIN_TERM = '{';
	private static final char END_TERM = '}';
	private static final char EL_DESIGNATOR = '$';
	private static final char ESCAPE_CHARACTER = '\\';


	private final String originalMessageDescriptor;
	private final InterpolationTermType interpolationTermType;

	private int currentPosition;
	private boolean inTerm;
	private boolean previousCharacterWasELDesignator;
	private boolean previousCharacterWasEscapeCharacter;
	private boolean interpolationComplete;
	private StringBuilder interpolatedMessage;
	private StringBuilder currentTerm;

	public static MessageDescriptorParser forExpressionLanguage(String messageDescriptor) {
		return new MessageDescriptorParser( messageDescriptor, InterpolationTermType.EL );
	}

	public static MessageDescriptorParser forParameter(String messageDescriptor) {
		return new MessageDescriptorParser( messageDescriptor, InterpolationTermType.PARAMETER );
	}

	private MessageDescriptorParser(String messageDescriptor, InterpolationTermType interpolationTermType) {
		this.originalMessageDescriptor = messageDescriptor;
		this.interpolationTermType = interpolationTermType;
		this.interpolatedMessage = new StringBuilder();
	}

	/**
	 * Called to advance the parsing of the message descriptor. This message can be called multiple times. Once it returns
	 * {@code false} the message parsing is complete and {@link #getInterpolatedMessage()} can be called.
	 *
	 * @return Returns {@code true} in case there are more message parameters, {@code false} otherwise.
	 *
	 * @throws MessageDescriptorFormatException Thrown in case the is an invalid interpolation term
	 */
	public boolean hasMoreInterpolationTerms() throws MessageDescriptorFormatException {
		while ( currentPosition < originalMessageDescriptor.length() ) {
			char currentCharacter = originalMessageDescriptor.charAt( currentPosition );
			switch ( currentCharacter ) {
				case BEGIN_TERM: {
					handleStartOfPotentialInterpolationTerm( currentCharacter );
					break;
				}
				case END_TERM: {
					if ( isEscapedState() ) {
						appendEscapedMetaCharacterAndLeaveEscapeState( currentCharacter );
					}
					else {
						if ( !inTerm ) {
							throw new MessageDescriptorFormatException( "The message descriptor '" + originalMessageDescriptor + "' contains an unterminated term." );
						}
						currentTerm.append( currentCharacter );
						inTerm = false;
						currentPosition++;
						return true;
					}
					break;
				}
				case EL_DESIGNATOR: {
					handleExpressionLanguageDesignator( currentCharacter );
					break;
				}
				case ESCAPE_CHARACTER: {
					interpolatedMessage.append( currentCharacter );
					previousCharacterWasEscapeCharacter = !previousCharacterWasEscapeCharacter;
					break;
				}
				default: {
					handleNonMetaCharacter( currentCharacter );
				}
			}
			currentPosition++;
		}

		if ( inTerm ) {
			throw new MessageDescriptorFormatException(
					"The message descriptor '" + originalMessageDescriptor + "' contains an unterminated parameter"
			);
		}

		interpolationComplete = true;
		return false;
	}

	private void handleNonMetaCharacter(char currentCharacter) throws MessageDescriptorFormatException {
		if ( previousCharacterWasELDesignator ) {
			throw new MessageDescriptorFormatException(
					"'$' needs to be escaped to be treated as literal in '" + originalMessageDescriptor + "'"
			);
		}
		if ( isEscapedState() ) {
			throw new MessageDescriptorFormatException(
					"The character '" + currentCharacter + "' in " + originalMessageDescriptor + " cannot be escaped."
			);
		}
		if ( inTerm ) {
			currentTerm.append( currentCharacter );
		}
		else {
			interpolatedMessage.append( currentCharacter );
		}
	}

	/**
	 * @return Returns the next interpolation term
	 */
	public String nextInterpolationTerm() {
		if ( currentTerm == null ) {
			throw new IllegalStateException( "Trying to access term without calling hasMoreInterpolationTerms" );
		}
		return currentTerm.toString();
	}

	/**
	 * Replaces the current interpolation term with the given string.
	 *
	 * @param replacement The string to replace the current term with.
	 */
	public void replaceCurrentInterpolationTerm(String replacement) {
		if ( currentTerm == null ) {
			throw new IllegalStateException( "Trying to replace term without calling hasMoreInterpolationTerms" );
		}
		currentTerm = null;
		interpolatedMessage.append( replacement );
	}

	public String getInterpolatedMessage() {
		if ( !interpolationComplete ) {
			throw new IllegalStateException( "Trying to access the interpolated message while still parsing terms" );
		}

		return interpolatedMessage.toString();
	}

	private void appendEscapedMetaCharacterAndLeaveEscapeState(char currentCharacter) {
		interpolatedMessage.append( currentCharacter );
		previousCharacterWasEscapeCharacter = false;
	}

	private boolean isEscapedState() {
		return previousCharacterWasEscapeCharacter;
	}

	private void startNewTerm(char currentCharacter) {
		inTerm = true;
		currentTerm = new StringBuilder();

		if ( previousCharacterWasELDesignator ) {
			if ( InterpolationTermType.EL.equals( interpolationTermType ) ) {
				currentTerm.append( EL_DESIGNATOR );
			}
			previousCharacterWasELDesignator = false;
		}

		currentTerm.append( currentCharacter );
	}

	private void handleExpressionLanguageDesignator(char currentCharacter) {
		if ( isEscapedState() ) {
			appendEscapedMetaCharacterAndLeaveEscapeState( currentCharacter );
		}
		else {
			if ( InterpolationTermType.PARAMETER.equals( interpolationTermType ) ) {
				interpolatedMessage.append( currentCharacter );
			}
			previousCharacterWasELDesignator = true;
		}
	}

	private void handleStartOfPotentialInterpolationTerm(char currentCharacter)
			throws MessageDescriptorFormatException {
		if ( inTerm ) {
			throw new MessageDescriptorFormatException(
					"The message descriptor '" + originalMessageDescriptor + "' contains nested parameters"
			);
		}
		if ( isEscapedState() ) {
			appendEscapedMetaCharacterAndLeaveEscapeState( currentCharacter );
		}
		else {
			startNewTerm( currentCharacter );
		}
	}

}


