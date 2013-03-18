/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
		if ( isDefinedOnSubType( method, otherMethod ) &&
				otherMethod.hasParameterConstraints() &&
				!method.isEquallyParameterConstrained( otherMethod ) ) {
			throw log.getParameterConfigurationAlteredInSubTypeException(
					method.getLocation().getMember(),
					otherMethod.getLocation().getMember()
			);
		}
	}
}
