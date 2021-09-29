/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml;

/**
 * Test class for HV-1853.
 *
 * @author Roman Vasilyev
 */
public class MultipleGetterCandidates {
	private Integer property1 = 1;
	private Integer property2 = 1;

	public boolean hasProperty1() {
		return property1 != null;
	}

	public Integer getProperty1() {
		return property1;
	}

	public boolean hasProperty2() {
		return property2 != null;
	}

	public Integer getProperty2() {
		return property2;
	}
}
