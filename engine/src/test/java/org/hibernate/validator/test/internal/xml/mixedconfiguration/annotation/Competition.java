/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml.mixedconfiguration.annotation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.test.internal.xml.mixedconfiguration.ICompetition;

public abstract class Competition implements ICompetition {

	@NotNull
	@Size(min = 1)
	private String name;

	public Competition() {
		super();
	}

	public Competition(String name) {
		setName( name );
	}

	public String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}
}
