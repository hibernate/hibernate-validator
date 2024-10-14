/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

import jakarta.validation.constraints.NotNull;

/**
 * @author Guillaume Smet
 */
public class Entity1 {

	@SuppressWarnings("unused")
	private Wrapper1<@NotNull String> wrapper;

	public Entity1(String value) {
		this.wrapper = new Wrapper1<String>( value );
	}
}
