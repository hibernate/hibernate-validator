/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.EndsWith;

public class ModelWithEndsWithConstraints {

	@EndsWith("foo")
	public Collection<String> collection;

	@EndsWith("foo")
	public List<String> list;

	@EndsWith("foo")
	public Set<String> set;

	@EndsWith("foo")
	public String string;
}
