/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.constraints.NullOrNotEmpty;

public class ModelWithNullOrNotEmptyConstraints {

	@NullOrNotEmpty
	public Integer integer;

	@NullOrNotEmpty
	public Boolean aBoolean;

	@NullOrNotEmpty
	public Double aDouble;

	@NullOrNotEmpty
	public String string;

	@NullOrNotEmpty
	public Collection<String> collection;

	@NullOrNotEmpty
	public List<String> list;

	@NullOrNotEmpty
	public Set<String> set;

	@NullOrNotEmpty
	public Map<String, String> map;

	@NullOrNotEmpty
	public String[] stringArray;

	@NullOrNotEmpty
	public int[] intArray;
}
