/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

import jakarta.validation.constraints.Size;
import jakarta.validation.valueextraction.Unwrapping;

/**
 * @author Gunnar Morling
 */
public class Order {

	@Size(min = 4, payload = { Unwrapping.Unwrap.class })
	private final Wrapper<Long> id = new Wrapper<Long>( 42L );
}
