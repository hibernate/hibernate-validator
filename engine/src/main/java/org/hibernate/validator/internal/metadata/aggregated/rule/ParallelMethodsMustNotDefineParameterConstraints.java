/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
		if ( isDefinedOnParallelType( method, otherMethod ) &&
				( method.hasParameterConstraints() || otherMethod.hasParameterConstraints() ) ) {
			throw LOG.getParameterConstraintsDefinedInMethodsFromParallelTypesException(
					method.getCallable(),
					otherMethod.getCallable()
			);
		}
	}
}
