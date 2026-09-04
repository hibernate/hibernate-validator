/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.Hexadecimal;

public class ModelWithHexadecimalConstraints {

	@Hexadecimal
	public String string;

	@Hexadecimal
	public StringBuilder stringBuilder;

	@Hexadecimal
	public Integer integer;

	@Hexadecimal
	public Collection<String> collection;

	@Hexadecimal
	public List<String> list;

	@Hexadecimal
	public Set<String> set;

}
