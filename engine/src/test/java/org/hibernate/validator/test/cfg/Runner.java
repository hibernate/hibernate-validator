/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cfg;

/**
 * @author Hardy Ferentschik
 */
public class Runner {

	private String name;

	private boolean paidEntryFee;

	private int age;

	public boolean isPaidEntryFee() {
		return paidEntryFee;
	}

	public void setPaidEntryFee(boolean paidEntryFee) {
		this.paidEntryFee = paidEntryFee;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

}
