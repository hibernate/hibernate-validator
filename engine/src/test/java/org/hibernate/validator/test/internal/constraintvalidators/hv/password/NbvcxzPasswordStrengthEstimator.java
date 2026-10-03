/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.password;

import org.hibernate.validator.spi.password.PasswordStrengthEstimator;
import org.hibernate.validator.spi.password.PasswordStrengthResult;

import me.gosimple.nbvcxz.Nbvcxz;
import me.gosimple.nbvcxz.resources.Feedback;
import me.gosimple.nbvcxz.scoring.Result;

class NbvcxzPasswordStrengthEstimator implements PasswordStrengthEstimator {

	private final Nbvcxz nbvcxz;

	NbvcxzPasswordStrengthEstimator() {
		this.nbvcxz = new Nbvcxz();
	}

	@Override
	public PasswordStrengthResult estimate(char[] password) {
		return estimate( new String( password ) );
	}

	@Override
	public PasswordStrengthResult estimate(String password) {
		Result result = nbvcxz.estimate( password );
		Feedback feedback = result.getFeedback();
		String warning = feedback != null ? feedback.getWarning() : null;
		return PasswordStrengthResult.simple( result.getBasicScore(), warning );
	}
}
