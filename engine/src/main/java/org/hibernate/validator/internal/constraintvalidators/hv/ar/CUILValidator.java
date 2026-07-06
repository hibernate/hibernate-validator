/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.ar;

import java.util.Set;

import org.hibernate.validator.constraints.ar.CUIL;

/**
 * Validator for {@link CUIL}.
 */
public class CUILValidator extends AbstractArgentineTaxIdValidator<CUIL> {

	private static final Set<String> PREFIXES = Set.of( "20", "23", "24", "25", "26", "27" );

	@Override
	protected Set<String> getValidPrefixes() {
		return PREFIXES;
	}
}
