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

