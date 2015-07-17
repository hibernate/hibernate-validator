/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.model;

import javax.validation.constraints.NotNull;

/**
 * @author Gunnar Morling
 */
public class Address {
	private final String city;

	public Address(String city) {
		this.city = city;
	}

	@NotNull
	public String getCity() {
		return city;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( city == null ) ? 0 : city.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		Address other = (Address) obj;
		if ( city == null ) {
			if ( other.city != null ) {
				return false;
			}
		}
		else if ( !city.equals( other.city ) ) {
			return false;
		}
		return true;
	}
}
