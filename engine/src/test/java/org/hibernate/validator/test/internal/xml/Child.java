/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml;

/**
 * Test class for HV-1534.
 *
 * @author robd
 */
public class Child extends Parent {

	String childAttribute;

	Child( String parentAttribute, String childAttribute ) {
		super( parentAttribute );
		this.childAttribute = childAttribute;
	}

	public final String getChildAttribute() {
		return childAttribute;
	}
}
