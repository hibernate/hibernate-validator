/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.metadata.aggregated.rule;

import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;

/**
 * Rule that ensures that overriding methods don't add to or alter the
 * constraints defined on the overridden method.
 *
 * @author Gunnar Morling
 */
public class OverridingMethodMustNotAlterParameterConstraints extends MethodConfigurationRule {

	@Override
	public void apply(ConstrainedExecutable method, ConstrainedExecutable otherMethod) {
		if ( isDefinedOnSubType( method, otherMethod )
				&& otherMethod.hasParameterConstraints()
				&& !method.isEquallyParameterConstrained( otherMethod ) ) {
			throw LOG.getParameterConfigurationAlteredInSubTypeException(
					method.getCallable(),
					otherMethod.getCallable()
			);
		}
	}
}
