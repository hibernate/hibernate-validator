/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
