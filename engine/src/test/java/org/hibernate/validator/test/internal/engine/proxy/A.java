/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.proxy;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

interface A {
	@Min(5)
	Integer getInteger();

	@Size(min = 2)
	String getString();
}
