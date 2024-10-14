/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
