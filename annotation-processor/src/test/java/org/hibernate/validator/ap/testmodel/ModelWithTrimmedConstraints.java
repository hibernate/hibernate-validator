/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.Trimmed;

public class ModelWithTrimmedConstraints {

	@Trimmed
	public Collection<String> collection;

	@Trimmed
	public List<String> list;

	@Trimmed
	public Set<String> set;

	@Trimmed
	public String string;
}
