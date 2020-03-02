/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
