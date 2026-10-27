/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.Encoding;

public class ModelWithEncodingConstraints {

	@Encoding(charset = "US-ASCII")
	public String string;

	@Encoding(charset = "US-ASCII")
	public StringBuilder stringBuilder;

	@Encoding(charset = "US-ASCII")
	public Integer integer;

	@Encoding(charset = "US-ASCII")
	public Collection<String> collection;

	@Encoding(charset = "US-ASCII")
	public List<String> list;

	@Encoding(charset = "US-ASCII")
	public Set<String> set;

}
