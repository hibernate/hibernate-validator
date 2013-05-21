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
 * Parsing a message descriptor results in multiple {@code Token}s. Each token
 * keeps track of its token value and whether the token is a parameter which can
 * be interpolated.
 *
 * @author Hardy Ferentschik
 */
public class Token {
	private boolean isParameter;
	private StringBuilder tokenValue;

	public Token(String tokenStart) {
		tokenValue = new StringBuilder();
		tokenValue.append( tokenStart );
	}

	public Token(char tokenStart) {
		this( String.valueOf( tokenStart ) );
	}

	public void append(char character) {
		tokenValue.append( character );
	}

	public void makeParameterToken(boolean parameter) {
		isParameter = parameter;
	}

	public boolean isParameter() {
		return isParameter;
	}

	public String getTokenValue() {
		return tokenValue.toString();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder( "Token{" );
		sb.append( "isParameter=" ).append( isParameter );
		sb.append( ", tokenValue='" ).append( tokenValue.toString() );
		sb.append( "'}" );
		return sb.toString();
	}
}


