/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.metadata;

import jakarta.validation.constraints.NotNull;

/**
 * @author Gunnar Morling
 */
public class ChildWithoutAtValid2 extends ParentWithoutAtValid {

	@Override
	@NotNull
	public Order getOrder() {
		return null;
	}

}
