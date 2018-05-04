/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraintvalidation;

import java.util.List;

import javax.validation.ConstraintValidatorContext;

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
	 * @return a list of currently validated method parameter names.
	 */
	List<String> getMethodParameterNames();
}
