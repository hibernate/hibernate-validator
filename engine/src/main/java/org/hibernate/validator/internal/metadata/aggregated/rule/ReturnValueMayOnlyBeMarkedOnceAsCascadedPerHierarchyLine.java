/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated.rule;

import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;

/**
 * Rule that ensures that the method return value is marked only once as
 * cascaded per hierarchy line.
 *
 * @author Gunnar Morling
 */
public class ReturnValueMayOnlyBeMarkedOnceAsCascadedPerHierarchyLine extends MethodConfigurationRule {

	@Override
	public void apply(ConstrainedExecutable method, ConstrainedExecutable otherMethod) {
		if ( method.getCascadingMetaDataBuilder().isMarkedForCascadingOnAnnotatedObjectOrContainerElements() &&
				otherMethod.getCascadingMetaDataBuilder().isMarkedForCascadingOnAnnotatedObjectOrContainerElements() &&
				( isDefinedOnSubType( method, otherMethod ) || isDefinedOnSubType( otherMethod, method ) ) ) {
			throw LOG.getMethodReturnValueMustNotBeMarkedMoreThanOnceForCascadedValidationException(
					method.getCallable(),
					otherMethod.getCallable()
			);
		}
	}
}
