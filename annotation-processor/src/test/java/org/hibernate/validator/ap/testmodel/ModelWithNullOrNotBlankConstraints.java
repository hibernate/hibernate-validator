/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.NullOrNotBlank;

public class ModelWithNullOrNotBlankConstraints {

	@NullOrNotBlank
	public Collection<String> collection;

	@NullOrNotBlank
	public List<String> list;

	@NullOrNotBlank
	public Set<String> set;

	@NullOrNotBlank
	public String string;
}
