/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.serialization;

import java.io.Serializable;
import java.util.Objects;

import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

/**
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class SerializableClass implements Serializable {

	@Size(min = 5, payload = TestPayload.class)
	private String foo;

	public SerializableClass(String foo) {
		this.foo = foo;
	}

	public void fooParameter(@Size(min = 5, payload = TestPayload.class) String foo) {
	}

	@Size(min = 5, payload = TestPayload.class)
	public String fooReturnValue() {
		return foo;
	}

	@Override
	public boolean equals(Object obj) {
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}

		SerializableClass other = (SerializableClass) obj;

		return Objects.equals( foo, other.foo );
	}

	@Override
	public int hashCode() {
		return Objects.hash( foo );
	}

	public static class TestPayload implements Payload {
	}
}
