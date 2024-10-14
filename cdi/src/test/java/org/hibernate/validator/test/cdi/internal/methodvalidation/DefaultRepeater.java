/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation;

import jakarta.validation.executable.ValidateOnExecution;

/**
 * @author Hardy Ferentschik
 */
@ValidateOnExecution
public class DefaultRepeater implements Repeater<String> {

	@Override
	public String repeat(String in) {
		return in;
	}

	@Override
	public String reverse(String in) {
		return null;
	}

	@Override
	public String getHelloWorld() {
		return null;
	}
}
