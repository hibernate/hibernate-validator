/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.ap.testmodel;

import java.util.Date;
import javax.validation.constraints.Size;

public class MethodLevelValidationUsingBuiltInConstraints {
	@Size(min = 10)
	public String getString() {
		return null;
	}

	/**
	 * Not allowed. Method is no getter.
	 */
	@Size(min = 10)
	public void setString() {
	}

	/**
	 * Not allowed. Return type doesn't match.
	 */
	@Size(min = 10)
	public Date getDate() {
		return null;
	}

	/**
	 * Not allowed. No return type.
	 */
	@Size(min = 10)
	public void getAnotherString() {
	}

	/**
	 * Not allowed. Static method.
	 */
	@Size(min = 10)
	public static String getStringStatically() {
		return null;
	}

	/**
	 * No getter, but allowed with -AmethodConstraintsSupported.
	 */
	@Size(min = 10)
	public String doSomething() {
		return null;
	}

	/**
	 * Also with -AmethodConstraintsSupported not allowed, as return type doesn't match.
	 */
	@Size(min = 10)
	public Date doSomethingReturningDate() {
		return null;
	}

	/**
	 * Also with -AmethodConstraintsSupported not allowed. No return type.
	 */
	@Size(min = 10)
	public void voidDoSomething() {
	}

	/**
	 * Also with -AmethodConstraintsSupported not allowed. Static method.
	 */
	@Size(min = 10)
	public static String staticDoSomething() {
		return null;
	}

}
