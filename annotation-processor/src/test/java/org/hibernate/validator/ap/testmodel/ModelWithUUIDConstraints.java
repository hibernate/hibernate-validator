/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import java.nio.CharBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.UUID;

public class ModelWithUUIDConstraints {

	@UUID
	public String string;

	@UUID
	public StringBuilder stringBuilder;

	@UUID
	public CharBuffer charBuffer;

	@UUID
	public java.util.UUID uuid;

	@UUID
	public Collection<String> collection;

	@UUID
	public List<String> list;

	@UUID
	public Set<String> set;

	@UUID
	private Integer integer;

}
