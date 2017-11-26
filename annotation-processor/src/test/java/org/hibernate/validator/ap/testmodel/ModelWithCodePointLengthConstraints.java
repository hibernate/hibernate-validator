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

import org.hibernate.validator.constraints.CodePointLength;

public class ModelWithCodePointLengthConstraints {

	@CodePointLength
	public Collection<String> collection;

	@CodePointLength
	public List<String> list;

	@CodePointLength
	public Set<String> set;

	@CodePointLength
	public String string;
}
