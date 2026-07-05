/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.ar;

import org.hibernate.validator.constraints.ar.CUIL;

/**
 * Validator for {@link CUIL}.
 */
public class CUILValidator extends AbstractArgentineTaxIdValidator<CUIL> {

	private static final String[] PREFIXES = { "20", "23", "24", "27" };

	@Override
	protected String[] getValidPrefixes() {
		return PREFIXES;
	}
}
