/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.messageinterpolation;

import java.util.Map;

import jakarta.validation.MessageInterpolator;
import jakarta.validation.Path;

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
}
