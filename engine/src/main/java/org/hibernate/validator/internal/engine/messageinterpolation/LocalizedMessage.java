/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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

