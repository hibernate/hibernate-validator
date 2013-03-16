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

import javax.validation.ConstraintDeclarationException;

import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;

/**
 * Rule that ensures that the method return value is marked only once as
 * cascaded per hierarchy line.
 *
 * @author Gunnar Morling
 */
public class ReturnValueMayOnlyBeMarkedOnceAsCascadedPerHierarchyLine extends MethodConfigurationRule {

	@Override
	public ConstraintDeclarationException apply(ConstrainedExecutable method, ConstrainedExecutable otherMethod) {
		if ( method.isCascading() && otherMethod.isCascading() &&
				( isDefinedOnSubType( method, otherMethod ) || isDefinedOnSubType( otherMethod, method ) ) ) {
			return log.methodReturnValueMustNotBeMarkedMoreThanOnceForCascadedValidation(
					method.getLocation().getMember(),
					otherMethod.getLocation().getMember()
			);
		}

		return null;
	}
}
