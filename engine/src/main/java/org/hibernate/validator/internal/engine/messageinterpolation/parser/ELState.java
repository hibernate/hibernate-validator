/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation.parser;

import static org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper.EL_DESIGNATOR;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Hardy Ferentschik
 */
public class ELState implements ParserState {
	private static final Log log = LoggerFactory.make();

	@Override
	public void terminate(TokenCollector tokenCollector) throws MessageDescriptorFormatException {
		tokenCollector.appendToToken( EL_DESIGNATOR );
		tokenCollector.terminateToken();
	}

	@Override
	public void handleNonMetaCharacter(char character, TokenCollector tokenCollector)
			throws MessageDescriptorFormatException {
		tokenCollector.appendToToken( EL_DESIGNATOR );
		tokenCollector.appendToToken( character );
		tokenCollector.terminateToken();
		tokenCollector.transitionState( new MessageState() );
	}

	@Override
	public void handleBeginTerm(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException {
		tokenCollector.terminateToken();

		tokenCollector.appendToToken( EL_DESIGNATOR );
		tokenCollector.appendToToken( character );
		tokenCollector.makeELToken();
		tokenCollector.transitionState( new InterpolationTermState() );
	}

	@Override
	public void handleEndTerm(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException {
		throw log.getNonTerminatedParameterException(
				tokenCollector.getOriginalMessageDescriptor(),
				character
		);
	}

	@Override
	public void handleEscapeCharacter(char character, TokenCollector tokenCollector)
			throws MessageDescriptorFormatException {
		tokenCollector.transitionState( new EscapedState( this ) );
	}

	@Override
	public void handleELDesignator(char character, TokenCollector tokenCollector)
			throws MessageDescriptorFormatException {
		handleNonMetaCharacter( character, tokenCollector );
	}
}
