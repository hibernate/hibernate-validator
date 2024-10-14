/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integrationtest.java.module.cdi.model;

import jakarta.validation.constraints.NotNull;

public class TestEntity {
	@NotNull
	private String foo;
}
