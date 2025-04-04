/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.metadata;

import jakarta.validation.Valid;

/**
 * @author Gunnar Morling
 */
public class ChildWithAtValid extends ParentWithoutAtValid {

	@Override
	@Valid
	public Order getOrder() {
		return null;
	}

}
