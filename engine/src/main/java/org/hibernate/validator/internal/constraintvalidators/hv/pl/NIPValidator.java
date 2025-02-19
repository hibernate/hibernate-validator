/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.pl;

import java.util.List;

import org.hibernate.validator.constraints.pl.NIP;

/**
 * Validator for {@link NIP}.
 *
 * @author Marko Bekhta
 */
public class NIPValidator extends PolishNumberValidator<NIP> {

	private static final int[] WEIGHTS_NIP = { 6, 5, 7, 2, 3, 4, 5, 6, 7 };


	@Override
	public void initialize(NIP constraintAnnotation) {
		super.initialize(
				0,
				Integer.MAX_VALUE,
				-1,
				true
		);
	}

	@Override
	protected int[] getWeights(List<Integer> digits) {
		return WEIGHTS_NIP;
	}
}
