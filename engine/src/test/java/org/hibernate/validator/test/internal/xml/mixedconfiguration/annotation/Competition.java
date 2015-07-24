/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml.mixedconfiguration.annotation;

import org.hibernate.validator.test.internal.xml.mixedconfiguration.ICompetition;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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

	public void setName(String name) {
		this.name = name;
	}
}
