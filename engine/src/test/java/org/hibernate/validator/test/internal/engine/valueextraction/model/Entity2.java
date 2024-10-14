/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

import jakarta.validation.constraints.NotNull;

/**
 * @author Guillaume Smet
 */
public class Entity2 {

	@SuppressWarnings("unused")
	private Wrapper2<@NotNull String> wrapper;

	public Entity2(String value) {
		this.wrapper = new Wrapper2<String>( value );
	}
}
