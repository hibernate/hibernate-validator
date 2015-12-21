/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.engine;

import javax.validation.ConstraintViolation;

/**
 * A custom {@link ConstraintViolation} which allows to get additional information for a constraint violation.
 *
 * @since 5.3
 */
public interface HibernateConstraintViolation<T> extends ConstraintViolation<T> {
	/**
	 * @return an object that further describes the violation or {@code null}.
	 */
	Object getViolationContext();
}
