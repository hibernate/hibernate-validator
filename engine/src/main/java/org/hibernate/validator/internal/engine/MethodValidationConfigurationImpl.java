/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import org.hibernate.validator.MethodValidationConfiguration;
import org.hibernate.validator.internal.metadata.aggregated.rule.MethodConfigurationRule;
import org.hibernate.validator.internal.metadata.aggregated.rule.OverridingMethodMustNotAlterParameterConstraints;
import org.hibernate.validator.internal.metadata.aggregated.rule.ParallelMethodsMustNotDefineGroupConversionForCascadedReturnValue;
import org.hibernate.validator.internal.metadata.aggregated.rule.ParallelMethodsMustNotDefineParameterConstraints;
import org.hibernate.validator.internal.metadata.aggregated.rule.ReturnValueMayOnlyBeMarkedOnceAsCascadedPerHierarchyLine;
import org.hibernate.validator.internal.metadata.aggregated.rule.VoidMethodsMustNotBeReturnValueConstrained;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
		
		if( ! this.isAllowOverridingMethodAlterParameterConstraint() )
			result.add( OverridingMethodMustNotAlterParameterConstraints.class );
		if( ! this.isAllowParallelMethodsDefineParameterConstraints() )
			result.add( ParallelMethodsMustNotDefineParameterConstraints.class );
		
		result.add( VoidMethodsMustNotBeReturnValueConstrained.class );
		
		if( ! this.isAllowMultipleCascadedValidationOnReturnValues() )
			result.add( ReturnValueMayOnlyBeMarkedOnceAsCascadedPerHierarchyLine.class );
		
		result.add( ParallelMethodsMustNotDefineGroupConversionForCascadedReturnValue.class );
		
		return Collections.unmodifiableSet( result );
	}
}
