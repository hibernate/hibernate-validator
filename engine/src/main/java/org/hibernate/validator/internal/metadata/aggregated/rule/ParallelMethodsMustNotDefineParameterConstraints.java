/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.metadata.aggregated.rule;

import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;

/**
 * Rule that ensures that parallel methods don't define any parameter
 * constraints.
 *
 * @author Gunnar Morling
 */
public class ParallelMethodsMustNotDefineParameterConstraints extends MethodConfigurationRule {

	@Override
	public void apply(ConstrainedExecutable method, ConstrainedExecutable otherMethod) {
		if ( isDefinedOnParallelType( method, otherMethod )
				&& ( method.hasParameterConstraints() || otherMethod.hasParameterConstraints() ) ) {
			throw LOG.getParameterConstraintsDefinedInMethodsFromParallelTypesException(
					method.getCallable(),
					otherMethod.getCallable()
			);
		}
	}
}
