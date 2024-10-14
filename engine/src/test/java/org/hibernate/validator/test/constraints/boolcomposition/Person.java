/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

// $Id: PersonBool.java 19547 2010-05-19 15:40:07Z hardy.ferentschik $

package org.hibernate.validator.test.constraints.boolcomposition;

/**
 * @author Hardy Ferentschik
 */
public class Person {
	@PatternOrSize
	private String nickName;

	@NotNullAndSize
	@PatternOrSize
	private String name;

	@AllowedSSN
	private String ssn = "";


	@ExcludedSSNList
	@IsBlank
	private String anotherSsn = "";

	public Person(String nickName, String name) {
		this.nickName = nickName;
		this.name = name;
	}

	public Person(String nickName, String name, String ssn) {
		this( nickName, name );
		this.ssn = ssn;
	}

	public Person(String nickName, String name, String ssn, String anotherSsn) {
		this( nickName, name, ssn );
		this.anotherSsn = anotherSsn;
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

	public String getSsn() {
		return ssn;
	}

	public void setSsn(String ssn) {
		this.ssn = ssn;
	}

	public String getAnotherSsn() {
		return anotherSsn;
	}

	public void setAnotherSsn(String anotherSsn) {
		this.anotherSsn = anotherSsn;
	}

}
