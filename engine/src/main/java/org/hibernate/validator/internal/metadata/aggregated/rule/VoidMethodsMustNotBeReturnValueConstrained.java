/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated.rule;

import java.lang.reflect.Method;

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
		if ( ( executable.getExecutable() instanceof Method ) &&
				( (Method) executable.getExecutable() ).getReturnType() == void.class &&
				( !executable.getConstraints().isEmpty() || executable.getCascadingMetaData().isMarkedForCascadingOnElementOrContainerElements() ) ) {
			throw log.getVoidMethodsMustNotBeConstrainedException( executable.getExecutable() );
		}
	}
}
