/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.traversableresolver;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.groups.Default;

/**
 * @author Emmanuel Bernard
 */
public class Trousers {

	@Min(value = 70, groups = { Default.class, Cloth.class })
	@Max(value = 220)
	private Integer length;

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}
}
