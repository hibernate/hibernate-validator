/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.ar;

import org.hibernate.validator.constraints.ar.CUIT;

/**
 * Validator for {@link CUIT}.
 */
public class CUITValidator extends AbstractArgentineTaxIdValidator<CUIT> {

	private static final String[] PREFIXES = { "20", "23", "24", "27", "30", "33", "34" };

	@Override
	protected String[] getValidPrefixes() {
		return PREFIXES;
	}
}
