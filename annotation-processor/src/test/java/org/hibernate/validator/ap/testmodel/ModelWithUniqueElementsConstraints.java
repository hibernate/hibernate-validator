/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.UniqueElements;

public class ModelWithUniqueElementsConstraints {

	@UniqueElements
	public Collection<String> collection;

	@UniqueElements
	public List<String> list;

	@UniqueElements
	public Set<String> set;

	@UniqueElements
	public String string;
}
