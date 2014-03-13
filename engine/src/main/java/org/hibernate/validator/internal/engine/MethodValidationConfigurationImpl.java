/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.engine;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.MethodValidationConfiguration;
import org.hibernate.validator.internal.metadata.aggregated.rule.MethodConfigurationRule;
import org.hibernate.validator.internal.metadata.aggregated.rule.OverridingMethodMustNotAlterParameterConstraints;
import org.hibernate.validator.internal.metadata.aggregated.rule.ParallelMethodsMustNotDefineGroupConversionForCascadedReturnValue;
import org.hibernate.validator.internal.metadata.aggregated.rule.ParallelMethodsMustNotDefineParameterConstraints;
import org.hibernate.validator.internal.metadata.aggregated.rule.ReturnValueMayOnlyBeMarkedOnceAsCascadedPerHierarchyLine;
import org.hibernate.validator.internal.metadata.aggregated.rule.VoidMethodsMustNotBeReturnValueConstrained;

/**
 * A class that maintains the configuration for method validation.
 * The configuration parameters allow for relaxation of rules associated
 * to the Liskov Substitution Principle.
 * 
 * @author Chris Beckey cbeckey@paypal.com
 *
 */
public class MethodValidationConfigurationImpl 
implements MethodValidationConfiguration {
	private boolean allowOverridingMethodAlterParameterConstraint = false;
	private boolean allowMultipleCascadedValidationOnReturnValues = false;
	private boolean allowParallelMethodsDefineParameterConstraints = false;

	@Override
	public MethodValidationConfiguration allowOverridingMethodAlterParameterConstraint(boolean allow) {
		this.allowOverridingMethodAlterParameterConstraint = allow;
		return this;
	}

	@Override
	public MethodValidationConfiguration allowMultipleCascadedValidationOnReturnValues(boolean allow) {
		this.allowMultipleCascadedValidationOnReturnValues = allow;
		return this;
	}

	@Override
	public MethodValidationConfiguration allowParallelMethodsDefineParameterConstraints(boolean allow) {
		this.allowParallelMethodsDefineParameterConstraints = allow;
		return this;
	}

	@Override
	public boolean isAllowOverridingMethodAlterParameterConstraint() {
		return this.allowOverridingMethodAlterParameterConstraint;
	}

	@Override
	public boolean isAllowMultipleCascadedValidationOnReturnValues() {
		return this.allowMultipleCascadedValidationOnReturnValues;
	}

	@Override
	public boolean isAllowParallelMethodsDefineParameterConstraints() {
		return this.allowParallelMethodsDefineParameterConstraints;
	}

	/**
	 * Return an unmodifiable Set of MethodConfigurationRule that are to be
	 * enforced based on the configuration. 
	 * @return
	 */
	public Set<Class<? extends MethodConfigurationRule>> getConfiguredRuleSet() {
		HashSet<Class<? extends MethodConfigurationRule>> result = new HashSet<Class<? extends MethodConfigurationRule>>();
		
		if(! this.isAllowOverridingMethodAlterParameterConstraint())
			result.add( OverridingMethodMustNotAlterParameterConstraints.class );
		if(! this.isAllowParallelMethodsDefineParameterConstraints())
			result.add( ParallelMethodsMustNotDefineParameterConstraints.class );
		
		result.add( VoidMethodsMustNotBeReturnValueConstrained.class );
		
		if(! this.isAllowMultipleCascadedValidationOnReturnValues())
			result.add( ReturnValueMayOnlyBeMarkedOnceAsCascadedPerHierarchyLine.class );
		
		result.add( ParallelMethodsMustNotDefineGroupConversionForCascadedReturnValue.class );
		
		return Collections.unmodifiableSet(result);
	}
}
