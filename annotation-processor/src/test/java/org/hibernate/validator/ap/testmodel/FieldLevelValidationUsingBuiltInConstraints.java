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
import java.util.Date;
import java.util.List;
import javax.validation.constraints.Size;

public class FieldLevelValidationUsingBuiltInConstraints {

	@Size(min = 10)
	public String string;

	@Size(min = 10)
	public Collection collection1;

	@Size(min = 10)
	public Collection<?> collection2;

	@Size(min = 10)
	public Collection<String> stringCollection;

	/**
	 * Allowed, as List extends Collection.
	 */
	@Size(min = 10)
	public List list1;

	@Size(min = 10)
	public List<?> list2;

	@Size(min = 10)
	public List<String> stringList;

	/**
	 * Not allowed (unsupported type).
	 */
	@Size(min = 10)
	public Date date;

	/**
	 * Not allowed (static field).
	 */
	@Size(min = 10)
	public static String staticString;

	@Size(min = 10)
	public Object[] objectArray;

	@Size(min = 10)
	public Integer[] integerArray;

	@Size(min = 10)
	public int[] intArray;
}
