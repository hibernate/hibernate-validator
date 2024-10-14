/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.boolcomposition.localconstrval;

/**
 * Test mode for HV-390.
 *
 * @author Federico Mancini
 * @author Dag Hovland
 */
public class Person {
	@SmallString
	private String nickName;

	@PatternOrLong
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
