/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.password;

import org.hibernate.validator.internal.util.actions.NewInstance;
import org.hibernate.validator.spi.password.PasswordPolicyDefinition;
import org.hibernate.validator.spi.password.PasswordPolicyDefinitionResolver;

public class DefaultPasswordPolicyDefinitionResolver implements PasswordPolicyDefinitionResolver {

	@Override
	public <T extends PasswordPolicyDefinition> T resolve(Class<T> definitionClass) {
		return NewInstance.action( definitionClass, "the password policy rule" );
	}
}
