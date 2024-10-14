/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.metadata;


/**
 * @author Gunnar Morling
 */
public class ChildWithoutAtValid extends ParentWithAtValid {

	@Override
	public Order getOrder() {
		return null;
	}

}
