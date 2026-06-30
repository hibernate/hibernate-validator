/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.DateTimeFormat;

/**
 * @author Sean Okafor
 */
public class ModelWithDateTimeFormatConstraints {
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private String string;

	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private CharSequence charSequence;

	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Integer integer;
}
