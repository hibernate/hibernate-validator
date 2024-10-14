/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.constraintvalidation;

import java.util.List;

import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.Incubating;

/**
 * A custom {@link ConstraintValidatorContext} which provides additional functionality for cross parameter validation contexts.
 *
 * @author Marko Bekhta
 * @since 6.1.0
 */
@Incubating
public interface HibernateCrossParameterConstraintValidatorContext extends HibernateConstraintValidatorContext {

	/**
	 * @return the list of the parameter names of the validated method.
	 */
	List<String> getMethodParameterNames();
}
