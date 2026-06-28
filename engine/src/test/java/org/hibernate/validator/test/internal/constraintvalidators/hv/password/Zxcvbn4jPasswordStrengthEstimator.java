/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.password;

import org.hibernate.validator.spi.password.PasswordStrengthEstimator;
import org.hibernate.validator.spi.password.PasswordStrengthResult;

import com.nulabinc.zxcvbn.Feedback;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

class Zxcvbn4jPasswordStrengthEstimator implements PasswordStrengthEstimator {

	private final Zxcvbn zxcvbn;

	Zxcvbn4jPasswordStrengthEstimator() {
		this.zxcvbn = new Zxcvbn();
	}

	@Override
	public PasswordStrengthResult estimate(char[] password) {
		return estimate( new String( password ) );
	}

	@Override
	public PasswordStrengthResult estimate(String password) {
		Strength strength = zxcvbn.measure( password );
		Feedback feedback = strength.getFeedback();
		String warning = feedback != null ? feedback.getWarning() : null;
		return PasswordStrengthResult.simple( strength.getScore(), warning );
	}

}
