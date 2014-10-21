/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.model;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author Gunnar Morling
 */
public class Customer {
	public final String name;
	private final Address address;

	public Customer(String name) {
		this( name, null );
	}

	public Customer(String name, Address address) {
		this.name = name;
		this.address = address;
	}

	@NotNull
	public String getName() {
		return name;
	}

	@Valid
	public Address getAddress() {
		return address;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( address == null ) ? 0 : address.hashCode() );
		result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
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
		Customer other = (Customer) obj;
		if ( address == null ) {
			if ( other.address != null ) {
				return false;
			}
		}
		else if ( !address.equals( other.address ) ) {
			return false;
		}
		if ( name == null ) {
			if ( other.name != null ) {
				return false;
			}
		}
		else if ( !name.equals( other.name ) ) {
			return false;
		}
		return true;
	}
}
