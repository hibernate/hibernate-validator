/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml;

import jakarta.validation.constraints.NotNull;

/**
 * @author Hardy Ferentschik
 */
public class Part {
	@NotNull
	String partId;

	public String getPartId() {
		return partId;
	}

	public void setPartId(String partId) {
		this.partId = partId;
	}
}
