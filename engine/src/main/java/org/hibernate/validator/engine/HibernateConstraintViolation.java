/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.engine;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

/**
 * A custom {@link ConstraintViolation} which allows to get additional information for a constraint violation.
 *
 * @param <T> The type of the root bean.
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
