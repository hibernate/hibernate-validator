/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml.mixedconfiguration.xml;

import org.hibernate.validator.test.internal.xml.mixedconfiguration.ICompetition;

public abstract class Competition implements ICompetition {

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
