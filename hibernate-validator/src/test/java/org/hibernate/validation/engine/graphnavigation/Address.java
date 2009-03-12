// $Id:$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine.graphnavigation;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validation.constraints.Length;

/**
 * @author Hardy Ferentschik
 */
public class Address {

	@NotNull
	@Length(max = 30)
	private String addressline1;

	private String zipCode;

	@Length(max = 30)
	@NotNull
	private String city;

	@Valid
	private User inhabitant;

	public Address() {
	}

	public Address(String addressline1, String zipCode, String city) {
		this.addressline1 = addressline1;
		this.zipCode = zipCode;
		this.city = city;
	}

	public String getAddressline1() {
		return addressline1;
	}

	public void setAddressline1(String addressline1) {
		this.addressline1 = addressline1;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public User getInhabitant() {
		return inhabitant;
	}

	public void setInhabitant(User inhabitant) {
		this.inhabitant = inhabitant;
	}
}
