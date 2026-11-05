package org.hibernate.validator.test.internal.engine.serialization;

import jakarta.validation.constraints.NotNull;

/**
 * @author Hardy Ferentschik
 */
public class UnSerializableClass {
	@NotNull
	private String foo;
}
