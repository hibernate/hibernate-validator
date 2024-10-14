/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.traversableresolver;

import jakarta.validation.constraints.Max;

/**
 * @author Emmanuel Bernard
 */
public class Jacket {
	Integer width;

	@Max(30)
	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}
}
