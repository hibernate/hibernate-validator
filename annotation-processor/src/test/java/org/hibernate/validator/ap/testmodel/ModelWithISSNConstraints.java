/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.ISSN;

/**
 * @author Andrea Boriero
 */
public class ModelWithISSNConstraints {

	@ISSN
	private String string;

	@ISSN
	private CharSequence charSequence;

	@ISSN
	private Integer integer;

}
