/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.metadata.aggregated.rule;

import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;

/**
 * Rule that ensures that void methods don't have any constraints or are marked
 * as cascaded.
 *
 * @author Gunnar Morling
 */
public class VoidMethodsMustNotBeReturnValueConstrained extends MethodConfigurationRule {

	@Override
	public void apply(ConstrainedExecutable executable, ConstrainedExecutable otherExecutable) {
		if ( !executable.getCallable().hasReturnValue()
				&& ( !executable.getConstraints().isEmpty() || executable.getCascadingMetaDataBuilder().isMarkedForCascadingOnAnnotatedObjectOrContainerElements() ) ) {
			throw LOG.getVoidMethodsMustNotBeConstrainedException( executable.getCallable() );
		}
	}
}
