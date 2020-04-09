/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties;

import java.util.Arrays;

public final class Signature {

	private final String name;

	private final Class<?>[] parameterTypes;

	public Signature(String name, Class<?>... parameterTypes) {
		this.name = name;
		this.parameterTypes = parameterTypes;
	}

	@Override
	public boolean equals(Object obj) {
		if ( obj == null ) {
			return false;
		}

		// we can safely assume the type will always be the right one
		Signature other = (Signature) obj;

		if ( !this.name.equals( other.name ) ) {
			return false;
		}

		return Arrays.equals( this.parameterTypes, other.parameterTypes );
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + Arrays.hashCode( parameterTypes );
		return result;
	}
}
