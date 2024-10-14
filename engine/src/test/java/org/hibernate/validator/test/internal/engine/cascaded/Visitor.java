/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.cascaded;

import jakarta.validation.constraints.NotNull;

/**
 * @author Gunnar Morling
 */
public class Visitor {

	@NotNull
	String name;

	Visitor() {
	}

	Visitor(String name) {
		this.name = name;
	}
}
