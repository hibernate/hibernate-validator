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

package org.hibernate.validator;

import java.util.Set;

import org.hibernate.validator.internal.metadata.aggregated.rule.MethodConfigurationRule;

/**
 * these properties modify the behavior of the Validator with respect to 
 * Specification section 4.5.5.
 * In particular: "Out of the box, a conforming Bean Validation provider must throw a 
 * ConstraintDeclarationException when discovering that any of these rules are violated. 
 * In addition providers may implement alternative, potentially more liberal, approaches 
 * for handling constrained methods in inheritance hierarchies. Possible means for activating 
 * such alternative behavior include provider-specific configuration properties or annotations. 
 * Note that client code relying on such alternative behavior is not portable between Bean 
 * Validation providers."
 * 
 * @author Chris Beckey cbeckey@paypal.com
 *
 */
public interface MethodValidationConfiguration {
	/**
	 * Property corresponding to the {@link #allowOverridingMethodAlterParameterConstraint} method.
	 * Accepts {@code true} or {@code false}. 
	 * Defaults to {@code false}.
	 */
	String ALLOW_PARAMETER_CONSTRAINT_OVERRIDE = "hibernate.validator.allow_parameter_constraint_override";
	
	/**
	 * Property corresponding to the {@link #allowParallelMethodsDefineGroupConversion} method.
	 * Accepts {@code true} or {@code false}. 
	 * Defaults to {@code false}.
	 */
	String ALLOW_PARALLEL_METHODS_DEFINE_GROUPS = "hibernate.validator.allow_parallel_methods_define_group";
	
	/**
	 * Property corresponding to the {@link #allowParallelMethodsDefineParameterConstraints} method.
	 * Accepts {@code true} or {@code false}. 
	 * Defaults to {@code false}.
	 */
	String ALLOW_PARALLEL_METHODS_DEFINE_PARAMETER_CONSTRAINTS = "hibernate.validator.allow_parallel_method_parameter_constraint";

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
