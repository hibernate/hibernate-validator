/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter09;

//end::include[]
import org.hibernate.validator.spi.password.PasswordStrengthEstimator;
import org.hibernate.validator.spi.password.PasswordStrengthResult;

import com.nulabinc.zxcvbn.Feedback;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

//tag::include[]
public class Zxcvbn4jPasswordStrengthEstimator implements PasswordStrengthEstimator {

	private final Zxcvbn zxcvbn = new Zxcvbn();

	@Override
	public PasswordStrengthResult estimate(char[] password) {
		Strength strength = zxcvbn.measure( new String( password ) );
		Feedback feedback = strength.getFeedback();
		String warning = feedback != null ? feedback.getWarning() : null;
		int score = strength.getScore();

		return PasswordStrengthResult.simple( score, warning );
	}
}
//end::include[]
