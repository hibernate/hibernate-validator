/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.messageinterpolation;

import java.util.Map;

import jakarta.validation.MessageInterpolator;
import jakarta.validation.Path;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.HibernateValidatorContext;
import org.hibernate.validator.Incubating;

/**
 * Extension to {@code MessageInterpolator.Context} which provides functionality
 * specific to Hibernate Validator.
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 * @since 5.0
 */
public interface HibernateMessageInterpolatorContext extends MessageInterpolator.Context {

	/**
	 * Returns the currently validated root bean type.
	 *
	 * @return The currently validated root bean type.
	 */
	Class<?> getRootBeanType();

	/**
	 * @return the message parameters added to this context for interpolation
	 *
	 * @since 5.4.1
	 */
	Map<String, Object> getMessageParameters();

	/**
	 * @return the expression variables added to this context for EL interpolation
	 *
	 * @since 5.4.1
	 */
	Map<String, Object> getExpressionVariables();

	/**
	 * @return the path to the validated constraint starting from the root bean
	 *
	 * @since 6.1
	 */
	Path getPropertyPath();

	/**
	 * @return the level of features enabled for the Expression Language engine
	 *
	 * @since 6.2
	 */
	ExpressionLanguageFeatureLevel getExpressionLanguageFeatureLevel();

	/**
	 * Returns an instance of the specified type or {@code null} if the constraint validator payload assigned to
	 * the validator isn't of the given type.
	 * <p>
	 * This is the payload configured through {@link HibernateValidatorConfiguration#constraintValidatorPayload(Object)}
	 * or {@link HibernateValidatorContext#constraintValidatorPayload(Object)}.
	 *
	 * @param type the type of payload to retrieve
	 * @return an instance of the specified type or {@code null} if the constraint validator payload isn't of the
	 * given type
	 *
	 * @since 9.2
	 */
	@Incubating
	<C> C getConstraintValidatorPayload(Class<C> type);
}
