/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.password;

import org.hibernate.validator.Incubating;

/**
 * Resolves {@link PasswordPolicyDefinition} instances from their class.
 * <p>
 * The default implementation uses the definition class's no-arg constructor.
 * A custom resolver can be registered via
 * {@link org.hibernate.validator.BaseHibernateValidatorConfiguration#addValidationService(Class, Object)
 * addValidationService(PasswordPolicyDefinitionResolver.class, resolver)}
 * to support dependency injection or other instantiation strategies.
 *
 * @since 9.2.0
 */
@Incubating
public interface PasswordPolicyDefinitionResolver {

	/**
	 * Resolves an instance of the given policy definition class.
	 *
	 * @param definitionClass the policy definition class to resolve
	 * @return the resolved instance
	 */
	<T extends PasswordPolicyDefinition> T resolve(Class<T> definitionClass);
}
