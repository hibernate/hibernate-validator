/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.cascadedgroupvalidation;

import jakarta.validation.constraints.NotNull;

/**
 * @author Jan-Willem Willebrands
 */
public class Entity {
	@NotNull(groups = ValidationGroup1.class)
	String value1;

	@NotNull(groups = ValidationGroup2.class)
	String value2;

	public Entity(String value1, String value2) {
		this.value1 = value1;
		this.value2 = value2;
	}
}
