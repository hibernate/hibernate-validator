/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation.parser;

import static org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper.BEGIN_TERM;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Hardy Ferentschik
 */
public class InterpolationTermState implements ParserState {
	private static final Log log = LoggerFactory.make();

	@Override
	public void terminate(TokenCollector tokenCollector) throws MessageDescriptorFormatException {
		throw log.getNonTerminatedParameterException(
				tokenCollector.getOriginalMessageDescriptor(),
				BEGIN_TERM
		);
	}

	@Override
	public void handleNonMetaCharacter(char character, TokenCollector tokenCollector)
			throws MessageDescriptorFormatException {
		tokenCollector.appendToToken( character );
	}

	@Override
	public void handleBeginTerm(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException {
		throw log.getNestedParameterException( tokenCollector.getOriginalMessageDescriptor() );
	}

	@Override
	public void handleEndTerm(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException {
		tokenCollector.appendToToken( character );
		tokenCollector.terminateToken();
		tokenCollector.transitionState( new MessageState() );
	}

	@Override
	public void handleEscapeCharacter(char character, TokenCollector tokenCollector)
			throws MessageDescriptorFormatException {
		tokenCollector.appendToToken( character );
		ParserState state = new EscapedState( this );
		tokenCollector.transitionState( state );
	}

	@Override
	public void handleELDesignator(char character, TokenCollector tokenCollector)
			throws MessageDescriptorFormatException {
		tokenCollector.appendToToken( character );
	}
}
