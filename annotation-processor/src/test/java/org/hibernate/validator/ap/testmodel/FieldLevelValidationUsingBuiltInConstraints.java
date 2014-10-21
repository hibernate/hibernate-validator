/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
