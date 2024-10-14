/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.metadata.aggregated.rule;

import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;

/**
 * Rule that ensures that methods in parallel types don't define group
 * conversions for methods marked as cascaded in one of the parallel types.
 *
 * @author Gunnar Morling
 */
public class ParallelMethodsMustNotDefineGroupConversionForCascadedReturnValue extends MethodConfigurationRule {

	@Override
	public void apply(ConstrainedExecutable method, ConstrainedExecutable otherMethod) {
		boolean isCascaded = method.getCascadingMetaDataBuilder().isMarkedForCascadingOnAnnotatedObjectOrContainerElements()
				|| otherMethod.getCascadingMetaDataBuilder().isMarkedForCascadingOnAnnotatedObjectOrContainerElements();
		boolean hasGroupConversions = method.getCascadingMetaDataBuilder().hasGroupConversionsOnAnnotatedObjectOrContainerElements()
				|| otherMethod.getCascadingMetaDataBuilder().hasGroupConversionsOnAnnotatedObjectOrContainerElements();

		if ( isDefinedOnParallelType( method, otherMethod ) && isCascaded && hasGroupConversions ) {
			throw LOG.getMethodsFromParallelTypesMustNotDefineGroupConversionsForCascadedReturnValueException(
					method.getCallable(),
					otherMethod.getCallable()
			);
		}
	}
}
