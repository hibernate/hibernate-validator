/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.password;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

/**
 * The result of a password strength estimation.
 *
 * @since 9.2.0
 */
@Incubating
public interface PasswordStrengthResult {

	static PasswordStrengthResult simple(int score, String feedback) {
		return new SimplePasswordStrengthResult( score, feedback );
	}

	/**
	 * @return the estimated strength score
	 */
	int score();

	/**
	 * @return optional human-readable feedback, or {@code null} if none
	 */
	String feedback();

	/**
	* @param minimumStrength the minimum strength score required to consider the password strong enough
	 * @return {@code true} if the password meets the requested minimum strength requirement,
	 * {@code false} otherwise.
	 */
	default boolean meetsMinimumStrength(int minimumStrength) {
		return score() >= minimumStrength;
	}

	default void addMessageParameters(HibernateConstraintValidatorContext constraintValidatorContext) {
		constraintValidatorContext
				.addMessageParameter( "score", score() );
		if ( feedback() != null ) {
			constraintValidatorContext
					.addMessageParameter( "feedback", feedback() );
		}
	}

	record SimplePasswordStrengthResult(int score, String feedback) implements PasswordStrengthResult {
	}
}
