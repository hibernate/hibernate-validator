/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.engine;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

/**
 * A custom {@link ConstraintViolation} which allows to get additional information for a constraint violation.
 *
 * @since 5.3
 */
public interface HibernateConstraintViolation<T> extends ConstraintViolation<T> {

	/**
	 * @param type The type of payload to retrieve
	 * @return an instance of the specified type set by the user via
	 * {@link HibernateConstraintValidatorContext#withDynamicPayload(Object)} or {@code null} if no constraint payload
	 * if the given type has been set.
	 */
	<C> C getDynamicPayload(Class<C> type);
}
