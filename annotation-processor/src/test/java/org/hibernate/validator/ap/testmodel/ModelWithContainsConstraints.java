/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.Contains;

public class ModelWithContainsConstraints {

	@Contains("foo")
	public Collection<String> collection;

	@Contains("foo")
	public List<String> list;

	@Contains("foo")
	public Set<String> set;

	@Contains("foo")
	public String string;
}
