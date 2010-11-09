/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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


	@Blacklist
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
