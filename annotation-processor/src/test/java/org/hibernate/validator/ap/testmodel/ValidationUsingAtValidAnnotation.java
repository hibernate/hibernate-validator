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

import java.util.Collection;
import javax.validation.Valid;

public class ValidationUsingAtValidAnnotation {

	@Valid
	public Integer integer;

	@Valid
	public Collection<?> collection1;

	/**
	 * Not allowed (primitive type).
	 */
	@Valid
	public int primitiveInt;

	/**
	 * Not allowed (static field).
	 */
	@Valid
	public static Integer staticInteger;

	@Valid
	public Integer getInteger() {
		return null;
	}

	@Valid
	public Collection<?> getCollection() {
		return null;
	}

	/**
	 * Not allowed (primitive type).
	 */
	@Valid
	public int getPrimitiveInt() {
		return 0;
	}

	/**
	 * Not allowed (static field).
	 */
	@Valid
	public static Integer getStaticInteger() {
		return null;
	}

	/**
	 * Not allowed (no getter).
	 */
	@Valid
	public Collection<?> getCollectionWithParams(int i) {
		return null;
	}

	/**
	 * Not allowed (no getter).
	 */
	@Valid
	public Collection<?> setCollection() {
		return null;
	}

	/**
	 * Not allowed (no getter).
	 */
	@Valid
	public void getVoid() {
	}
}
