/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.UpperCase;

public class ModelWithUpperCaseConstraints {

	@UpperCase
	public Collection<String> collection;

	@UpperCase
	public List<String> list;

	@UpperCase
	public Set<String> set;

	@UpperCase
	public String string;
}
