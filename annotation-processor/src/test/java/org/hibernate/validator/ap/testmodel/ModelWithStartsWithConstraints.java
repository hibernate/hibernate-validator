/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.StartsWith;

public class ModelWithStartsWithConstraints {

	@StartsWith("foo")
	public Collection<String> collection;

	@StartsWith("foo")
	public List<String> list;

	@StartsWith("foo")
	public Set<String> set;

	@StartsWith("foo")
	public String string;
}