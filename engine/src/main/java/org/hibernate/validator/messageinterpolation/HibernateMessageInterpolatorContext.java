/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.messageinterpolation;

import javax.validation.MessageInterpolator;

/**
 * Extension to {@code MessageInterpolator.Context} which provides functionality
 * specific to Hibernate Validator.
 *
 * @author Gunnar Morling
 * @since 5.0
 */
public interface HibernateMessageInterpolatorContext extends MessageInterpolator.Context {

	/**
	 * Returns the currently validated root bean type.
	 *
	 * @return The currently validated root bean type.
	 */
	Class<?> getRootBeanType();
}
