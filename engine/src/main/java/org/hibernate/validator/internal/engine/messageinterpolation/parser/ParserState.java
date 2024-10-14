/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation.parser;

/**
 * Interface defining the different methods a parser state has to respond to. It is up to the implementing state
 * to decide how to handle the different life cycle and callback methods
 *
 * @author Hardy Ferentschik
 */
public interface ParserState {

	void terminate(TokenCollector tokenCollector) throws MessageDescriptorFormatException;

	void handleNonMetaCharacter(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException;

	void handleBeginTerm(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException;

	void handleEndTerm(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException;

	void handleEscapeCharacter(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException;

	void handleELDesignator(char character, TokenCollector tokenCollector) throws MessageDescriptorFormatException;
}
