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

import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTermType;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Hardy Ferentschik
 */
public class BeginState implements ParserState {
	private static final Log log = LoggerFactory.make();

	@Override
	public void terminate(TokenCollector tokenCollector) throws MessageDescriptorFormatException {
	}

	@Override
	public void start(TokenCollector tokenCollector) throws MessageDescriptorFormatException {
		tokenCollector.next();
	}

	@Override
	public void handleNonMetaCharacter(char character, TokenCollector tokenCollector)
			throws MessageDescriptorFormatException {
		tokenCollector.appendToToken( character );
		tokenCollector.transitionState( new MessageState() );
		tokenCollector.next();
	}

	@Override
	public void handleBeginTerm(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException {
		// terminate a potential current token prior to the beginning of a new term
		tokenCollector.terminateToken();

		tokenCollector.appendToToken( character );
		if ( tokenCollector.getInterpolationType().equals( InterpolationTermType.PARAMETER ) ) {
			tokenCollector.makeParameterToken();
		}
		tokenCollector.transitionState( new InterpolationTermState() );
		tokenCollector.next();
	}

	@Override
	public void handleEndTerm(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException {
		throw log.getNonTerminatedParameterException( tokenCollector.getOriginalMessageDescriptor(), character );
	}

	@Override
	public void handleEscapeCharacter(char character, TokenCollector tokenCollector)
			throws MessageDescriptorFormatException {
		tokenCollector.appendToToken( character );
		tokenCollector.transitionState( new EscapedState( this ) );
		tokenCollector.next();
	}

	@Override
	public void handleELDesignator(char character, TokenCollector tokenCollector)
			throws MessageDescriptorFormatException {
		if ( tokenCollector.getInterpolationType().equals( InterpolationTermType.PARAMETER ) ) {
			handleNonMetaCharacter( character, tokenCollector );
		}
		else {
			ParserState state = new ELState();
			tokenCollector.transitionState( state );
			tokenCollector.next();
		}
	}
}


