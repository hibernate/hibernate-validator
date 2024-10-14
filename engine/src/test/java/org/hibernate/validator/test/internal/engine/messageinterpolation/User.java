/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import jakarta.validation.constraints.Email;

import org.hibernate.validator.constraints.Range;

/**
 * @author Hardy Ferentschik
 */
public class User {
	@Email
	private String email;

	@Range(min = 18, max = 21)
	private int age;

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
