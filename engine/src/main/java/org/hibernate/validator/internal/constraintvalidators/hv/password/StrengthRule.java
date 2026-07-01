/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.spi.password.PasswordPolicyRule;
import org.hibernate.validator.spi.password.PasswordStrengthEstimator;
import org.hibernate.validator.spi.password.PasswordStrengthResult;

class StrengthRule implements PasswordPolicyRule {

	private static final String MESSAGE = "{org.hibernate.validator.constraints.PasswordPolicy.strengthEstimator.message}";

	private final int minScore;
	private final PasswordStrengthEstimator estimator;

	StrengthRule(int minScore, PasswordStrengthEstimator estimator) {
		this.minScore = minScore;
		this.estimator = estimator;
	}

	@Override
	public String getMessage() {
		return MESSAGE;
	}

	@Override
	public boolean isValid(char[] password, HibernateConstraintValidatorContext context) {
		PasswordStrengthResult result = estimator.estimate( password );
		if ( result.score() >= minScore ) {
			return true;
		}
		context.addMessageParameter( "score", result.score() );
		context.addMessageParameter( "min", minScore );
		if ( result.feedback() != null ) {
			context.addMessageParameter( "feedback", result.feedback() );
		}

		return false;
	}
}
