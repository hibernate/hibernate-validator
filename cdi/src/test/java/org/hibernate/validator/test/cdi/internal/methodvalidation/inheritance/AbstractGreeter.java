/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

/**
 * @author Hardy Ferentschik
 */
public abstract class AbstractGreeter implements Greeter {
	@Override
	public String greet(String greeting) {
		return "Hello" + getName() + ", " + greeting;
	}

	protected abstract String getName();
}
