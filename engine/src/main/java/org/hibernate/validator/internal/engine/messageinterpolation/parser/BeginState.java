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
	public void terminate(ParserContext context) throws MessageDescriptorFormatException {
	}

	@Override
	public void start(ParserContext context) throws MessageDescriptorFormatException {
		context.next();
	}

	@Override
	public void handleNonMetaCharacter(char character, ParserContext context)
			throws MessageDescriptorFormatException {
		context.appendToToken( character );
		context.transitionState( new MessageState() );
		context.next();
	}

	@Override
	public void handleBeginTerm(char character, ParserContext context) throws MessageDescriptorFormatException {
		context.appendToToken( character );
		if ( context.getInterpolationType().equals( InterpolationTermType.PARAMETER ) ) {
			context.makeParameterToken();
		}
		context.transitionState( new InterpolationTermState() );
		context.next();
	}

	@Override
	public void handleEndTerm(char character, ParserContext context) throws MessageDescriptorFormatException {
		throw log.getNonTerminatedParameterException( context.getOriginalMessageDescriptor(), character );
	}

	@Override
	public void handleEscapeCharacter(char character, ParserContext context)
			throws MessageDescriptorFormatException {
		context.appendToToken( character );
		context.transitionState( new EscapedState( this ) );
		context.next();
	}

	@Override
	public void handleELDesignator(char character, ParserContext context) throws MessageDescriptorFormatException {
		if ( context.getInterpolationType().equals( InterpolationTermType.PARAMETER ) ) {
			handleNonMetaCharacter( character, context );
		}
		else {
			ParserState state = new ELState();
			context.transitionState( state );
			context.next();
		}
	}
}


