/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.validator.spi.password.PasswordContext;

class DefaultPasswordContext implements PasswordContext {

	private final char[] password;
	private Map<String, Object> properties;

	DefaultPasswordContext(char[] password) {
		this.password = password;
	}

	@Override
	public char[] password() {
		return password;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String name, Class<T> type) {
		if ( properties == null ) {
			return null;
		}
		Object value = properties.get( name );
		if ( value != null && type.isInstance( value ) ) {
			return (T) value;
		}
		return null;
	}

	@Override
	public PasswordContext property(String name, Object value) {
		if ( properties == null ) {
			properties = new HashMap<>();
		}
		properties.put( name, value );
		return this;
	}
}
