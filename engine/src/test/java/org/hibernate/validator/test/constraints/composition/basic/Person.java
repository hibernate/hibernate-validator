/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.composition.basic;

/**
 * Test mode for HV-182.
 *
 * @author Gerhard Petracek
 * @author Hardy Ferentschik
 */

public class Person {
	@ValidNameSingleViolation
	private String nickName;

	@ValidName
	private String name;

	public Person(String nickName, String name) {
		this.nickName = nickName;
		this.name = name;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
