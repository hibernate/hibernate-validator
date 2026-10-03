/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.password;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

/**
 * The result of a compromised-password check.
 *
 * @since 9.2.0
 */
@Incubating
public interface CompromisedPasswordResult {

	static CompromisedPasswordResult simple(int occurrences) {
		return new SimpleCompromisedPasswordResult( occurrences > 0, occurrences );
	}

	static CompromisedPasswordResult simple(boolean compromised, int occurrences) {
		return new SimpleCompromisedPasswordResult( compromised, occurrences );
	}

	/**
	 * @return {@code true} if the password was found in a breach dataset
	 */
	boolean compromised();

	/**
	 * @return the number of times the password was seen in breaches, or {@code -1} if unknown
	 */
	int occurrences();

	default void addMessageParameters(HibernateConstraintValidatorContext hibernateConstraintValidatorContext) {
		if ( occurrences() >= 0 ) {
			hibernateConstraintValidatorContext
					.addMessageParameter( "occurrences", occurrences() );
		}
	}

	record SimpleCompromisedPasswordResult(boolean compromised, int occurrences) implements CompromisedPasswordResult {
	}
}
