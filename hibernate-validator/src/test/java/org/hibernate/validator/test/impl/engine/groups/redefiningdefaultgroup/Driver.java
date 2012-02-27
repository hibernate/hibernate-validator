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
package org.hibernate.validator.test.impl.engine.groups.redefiningdefaultgroup;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;

/**
 * @author Hardy Ferentschik
 */
public class Driver extends Person {
	@Min(value = 18, message = "You have to be 18 to drive a car", groups = DriverChecks.class)
	private int age;

	@AssertTrue(message = "You first have to pass the driving test", groups = DriverChecks.class)
	private boolean hasDrivingLicense;

	public Driver(String name) {
		super( name );
	}

	public void passedDrivingTest(boolean b) {
		hasDrivingLicense = b;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
}
