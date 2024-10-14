/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
