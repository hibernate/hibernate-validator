/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator;

import jakarta.validation.ValidatorFactory;

/**
 * Provides Hibernate Validator extensions to {@link ValidatorFactory} in the context of a predefined scope.
 *
 * @since 6.1
 */
@Incubating
public interface PredefinedScopeHibernateValidatorFactory extends HibernateValidatorFactory {
}
