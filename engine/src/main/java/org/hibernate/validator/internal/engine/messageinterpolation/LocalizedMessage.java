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

import java.util.Locale;

/**
 * @author Hardy Ferentschik
 */
public class LocalizedMessage {
	private final String message;
	private final Locale locale;

	public LocalizedMessage(String message, Locale locale) {
		this.message = message;
		this.locale = locale;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		LocalizedMessage that = (LocalizedMessage) o;

		if ( locale != null ? !locale.equals( that.locale ) : that.locale != null ) {
			return false;
		}
		if ( message != null ? !message.equals( that.message ) : that.message != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = message != null ? message.hashCode() : 0;
		result = 31 * result + ( locale != null ? locale.hashCode() : 0 );
		return result;
	}
}

