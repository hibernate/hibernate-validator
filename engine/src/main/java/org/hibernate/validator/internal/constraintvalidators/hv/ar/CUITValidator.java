/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.ar;

import java.util.Set;

import org.hibernate.validator.constraints.ar.CUIT;

/**
 * Validator for {@link CUIT}.
 */
public class CUITValidator extends AbstractArgentineTaxIdValidator<CUIT> {

	private static final Set<String> PREFIXES = Set.of( "20", "23", "24", "25", "26", "27", "30", "33", "34" );

	@Override
	protected Set<String> getValidPrefixes() {
		return PREFIXES;
	}
}
