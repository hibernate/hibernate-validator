/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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

	public void setName(String name) {
		this.name = name;
	}
}
