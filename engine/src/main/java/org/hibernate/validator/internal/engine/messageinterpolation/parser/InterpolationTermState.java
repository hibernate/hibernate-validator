/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation.parser;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Hardy Ferentschik
 */
public class InterpolationTermState implements ParserState {
	private static final Log log = LoggerFactory.make();

	@Override
	public void start(TokenCollector tokenCollector) {
		throw new IllegalStateException( "Parsing of message descriptor cannot start in this state" );
	}

	@Override
	public void terminate(TokenCollector tokenCollector) throws MessageDescriptorFormatException {
		throw log.getNonTerminatedParameterException(
				tokenCollector.getOriginalMessageDescriptor(),
				TokenCollector.BEGIN_TERM
		);
	}

	@Override
	public void handleNonMetaCharacter(char character, TokenCollector tokenCollector)
			throws MessageDescriptorFormatException {
		tokenCollector.appendToToken( character );
		tokenCollector.next();
	}

	@Override
	public void handleBeginTerm(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException {
		throw log.getNestedParameterException( tokenCollector.getOriginalMessageDescriptor() );
	}

	@Override
	public void handleEndTerm(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException {
		tokenCollector.appendToToken( character );
		tokenCollector.terminateToken();
		BeginState beginState = new BeginState();
		tokenCollector.transitionState( beginState );
		tokenCollector.next();
	}

	@Override
	public void handleEscapeCharacter(char character, TokenCollector tokenCollector)
			throws MessageDescriptorFormatException {
		tokenCollector.appendToToken( character );
		ParserState state = new EscapedState( this );
		tokenCollector.transitionState( state );
		tokenCollector.next();

	}

	@Override
	public void handleELDesignator(char character, TokenCollector tokenCollector)
			throws MessageDescriptorFormatException {
		tokenCollector.appendToToken( character );
		tokenCollector.next();
	}
}


