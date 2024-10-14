/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.serialization;

import jakarta.validation.constraints.NotNull;

/**
 * @author Hardy Ferentschik
 */
public class UnSerializableClass {
	@NotNull
	private String foo;
}
