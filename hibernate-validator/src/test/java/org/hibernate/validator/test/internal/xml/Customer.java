/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.xml;

/**
 * @author Hardy Ferentschik
 */
public class Customer implements Person {
    private String firstName;
    private String middleName;
    private String lastName;

    private String customerId;

    private String password;

    public String getFirstName() {
	return this.firstName;
    }

    public void setFirstName(final String firstName) {
	this.firstName = firstName;
    }

    public String getMiddleName() {
	return this.middleName;
    }

    public void setMiddleName(final String middleName) {
	this.middleName = middleName;
    }

    public String getLastName() {
	return this.lastName;
    }

    public void setLastName(final String lastName) {
	this.lastName = lastName;
    }

    public String getCustomerId() {
	return this.customerId;
    }

    public void setCustomerId(final String customerId) {
	this.customerId = customerId;
    }

    public String getPassword() {
	return this.password;
    }

    public void setPassword(final String password) {
	this.password = password;
    }
}
