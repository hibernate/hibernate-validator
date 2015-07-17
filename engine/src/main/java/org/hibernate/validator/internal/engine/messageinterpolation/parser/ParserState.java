/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation.parser;

/**
 * Interface defining the different methods a parser state has to respond to. It is up to the implementing state
 * to decide how to handle the different life cycle and callback methods
 *
 * @author Hardy Ferentschik
 */
public interface ParserState {
	void start(TokenCollector tokenCollector) throws MessageDescriptorFormatException;

	void terminate(TokenCollector tokenCollector) throws MessageDescriptorFormatException;

	void handleNonMetaCharacter(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException;

	void handleBeginTerm(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException;

	void handleEndTerm(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException;

	void handleEscapeCharacter(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException;

	void handleELDesignator(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException;
}

