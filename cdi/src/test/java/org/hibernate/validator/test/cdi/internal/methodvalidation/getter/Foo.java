/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.getter;

import jakarta.validation.constraints.NotNull;

/**
 * @author Hardy Ferentschik
 */
public interface Foo {
	@NotNull
	String getFoo();
}
