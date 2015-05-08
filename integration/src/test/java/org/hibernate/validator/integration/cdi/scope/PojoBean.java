/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.cdi.scope;

public class PojoBean {
	// this field is differently configured via @Min resp @Max constraints depending on the containing EJB jar
	private int value;

	public PojoBean(int initValue) {
		this.value = initValue;
	}
}
