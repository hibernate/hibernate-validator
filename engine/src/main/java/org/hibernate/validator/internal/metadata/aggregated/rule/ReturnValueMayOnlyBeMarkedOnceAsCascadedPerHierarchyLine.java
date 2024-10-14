/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
		if ( method.getCascadingMetaDataBuilder().isMarkedForCascadingOnAnnotatedObjectOrContainerElements()
				&& otherMethod.getCascadingMetaDataBuilder().isMarkedForCascadingOnAnnotatedObjectOrContainerElements()
				&& ( isDefinedOnSubType( method, otherMethod ) || isDefinedOnSubType( otherMethod, method ) ) ) {
			throw LOG.getMethodReturnValueMustNotBeMarkedMoreThanOnceForCascadedValidationException(
					method.getCallable(),
					otherMethod.getCallable()
			);
		}
	}
}
