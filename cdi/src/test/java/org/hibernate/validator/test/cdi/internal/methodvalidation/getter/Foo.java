package org.hibernate.validator.test.cdi.internal.methodvalidation.getter;

import jakarta.validation.constraints.NotNull;

/**
 * @author Hardy Ferentschik
 */
public interface Foo {
	@NotNull
	String getFoo();
}
