/**
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator;

import org.hibernate.validator.internal.metadata.aggregated.rule.MethodConfigurationRule;

import java.util.Set;

/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

public interface MethodValidationConfiguration {
	/**
	 * Define whether overriding methods that override constraints should throw a ConstraintDefinitionException. The
	 * default value is FALSE, i.e. do not allow.
	 * 
	 * See Section 4.5.5 of JSR-349 Specification, specifically
	 * "In sub types (be it sub classes/interfaces or interface implementations), no parameter constraints may 
	 * be declared on overridden or implemented methods, nor may parameters be marked for cascaded validation. 
	 * This would pose a strengthening of preconditions to be fulfilled by the caller."
	 *  
	 * @param allow
	 * @return
	 */
	MethodValidationConfiguration allowOverridingMethodAlterParameterConstraint(boolean allow);
	boolean isAllowOverridingMethodAlterParameterConstraint();
	
	/**
	 * Define whether more than one constraint on a return value may be marked for cascading validation are allowed. 
	 * The default value is FALSE, i.e. do not allow.
	 * 
	 * "One must not mark a method return value for cascaded validation more than once in a line of a class hierarchy. 
	 * In other words, overriding methods on sub types (be it sub classes/interfaces or interface implementations) 
	 * cannot mark the return value for cascaded validation if the return value has already been marked on the 
	 * overridden method of the super type or interface."
	 * 
	 * @param allow
	 * @return
	 */
	MethodValidationConfiguration allowMultipleCascadedValidationOnReturnValues(boolean allow);
	boolean isAllowMultipleCascadedValidationOnReturnValues();
	
	/**
	 * Define whether parallel methods that define constraints should throw a ConstraintDefinitionException. The
	 * default value is FALSE, i.e. do not allow.
	 * 
	 * See Section 4.5.5 of JSR-349 Specification, specifically
	 * "If a sub type overrides/implements a method originally defined in several parallel types of the hierarchy 
	 * (e.g. two interfaces not extending each other, or a class and an interface not implemented by said class), 
	 * no parameter constraints may be declared for that method at all nor parameters be marked for cascaded validation. 
	 * This again is to avoid an unexpected strengthening of preconditions to be fulfilled by the caller."
	 * @param allow
	 * @return
	 */
	MethodValidationConfiguration allowParallelMethodsDefineParameterConstraints(boolean allow);
	boolean isAllowParallelMethodsDefineParameterConstraints();

	/**
	 * Return an unmodifiable Set of MethodConfigurationRule based on the configuration
	 * in this class.
	 * 
	 * @return
	 */
	Set<Class<? extends MethodConfigurationRule>> getConfiguredRuleSet();
}
