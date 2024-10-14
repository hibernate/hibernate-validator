/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation;

import java.util.Locale;

/**
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class LocalizedMessage {
	private final String message;
	private final Locale locale;
	private final int hashCode;

	public LocalizedMessage(String message, Locale locale) {
		this.message = message;
		this.locale = locale;
		this.hashCode = buildHashCode();
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

		if ( !message.equals( that.message ) ) {
			return false;
		}
		if ( !locale.equals( that.locale ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	private int buildHashCode() {
		int result = message.hashCode();
		result = 31 * result + locale.hashCode();
		return result;
	}
}
