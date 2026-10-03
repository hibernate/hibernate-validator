/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.NotCompromised;

public class ModelWithNotCompromisedConstraints {

	@NotCompromised
	private String string;

	@NotCompromised
	private char[] charArray;

	@NotCompromised
	private Integer integer;
}
