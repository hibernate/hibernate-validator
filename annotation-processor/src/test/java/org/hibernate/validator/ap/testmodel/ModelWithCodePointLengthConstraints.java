/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.CodePointLength;

public class ModelWithCodePointLengthConstraints {

	@CodePointLength
	public Collection<String> collection;

	@CodePointLength
	public List<String> list;

	@CodePointLength
	public Set<String> set;

	@CodePointLength
	public String string;
}
