/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
