/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.wildfly;

import org.hibernate.validator.integration.util.AcmeConstraint;
import org.hibernate.validator.integration.util.OxBerryConstraint;

/**
 * @author Hardy Ferentschik
 */
public class TestEntity {

	public TestEntity(String name) {
		this.name = name;
	}

	@OxBerryConstraint
	@AcmeConstraint
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
