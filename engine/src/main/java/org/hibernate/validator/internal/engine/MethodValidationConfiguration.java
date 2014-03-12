/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.internal.metadata.aggregated.rule.MethodConfigurationRule;
import org.hibernate.validator.internal.metadata.aggregated.rule.OverridingMethodMustNotAlterParameterConstraints;
import org.hibernate.validator.internal.metadata.aggregated.rule.ParallelMethodsMustNotDefineGroupConversionForCascadedReturnValue;
import org.hibernate.validator.internal.metadata.aggregated.rule.ParallelMethodsMustNotDefineParameterConstraints;
import org.hibernate.validator.internal.metadata.aggregated.rule.ReturnValueMayOnlyBeMarkedOnceAsCascadedPerHierarchyLine;
import org.hibernate.validator.internal.metadata.aggregated.rule.VoidMethodsMustNotBeReturnValueConstrained;

/**
 * These properties modify the behavior of the {@code }Validator} with respect to the Bean Validation
 * specification section 4.5.5. In particular:
 * <pre>
 * "Out of the box, a conforming Bean Validation provider must throw a
 * ConstraintDeclarationException when discovering that any of these rules are violated.
 * In addition providers may implement alternative, potentially more liberal, approaches
 * for handling constrained methods in inheritance hierarchies. Possible means for activating
 * such alternative behavior include provider-specific configuration properties or annotations.
 * Note that client code relying on such alternative behavior is not portable between Bean
 * Validation providers."
 * </pre>
 *
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
 */
public class MethodValidationConfiguration {
	private boolean allowOverridingMethodAlterParameterConstraint = false;
	private boolean allowMultipleCascadedValidationOnReturnValues = false;
	private boolean allowParallelMethodsDefineParameterConstraints = false;

	/**
	 * Define whether overriding methods that override constraints should throw a {@code ConstraintDefinitionException}.
	 * The default value is {@code false}, i.e. do not allow.
	 *
	 * See Section 4.5.5 of JSR-349 Specification, specifically
	 * <pre>
	 * "In sub types (be it sub classes/interfaces or interface implementations), no parameter constraints may
	 * be declared on overridden or implemented methods, nor may parameters be marked for cascaded validation.
	 * This would pose a strengthening of preconditions to be fulfilled by the caller."
	 * </pre>
	 *
	 * @param allow flag determining whether validation will allow overriding to alter parameter constraints.
	 *
	 * @return {@code this} following the chaining method pattern
	 */
	public MethodValidationConfiguration allowOverridingMethodAlterParameterConstraint(boolean allow) {
		this.allowOverridingMethodAlterParameterConstraint = allow;
		return this;
	}

	/**
	 * Define whether more than one constraint on a return value may be marked for cascading validation are allowed.
	 * The default value is {@code false}, i.e. do not allow.
	 *
	 * "One must not mark a method return value for cascaded validation more than once in a line of a class hierarchy.
	 * In other words, overriding methods on sub types (be it sub classes/interfaces or interface implementations)
	 * cannot mark the return value for cascaded validation if the return value has already been marked on the
	 * overridden method of the super type or interface."
	 *
	 * @param allow flag determining whether validation will allow multiple cascaded validation on return values.
	 *
	 * @return {@code this} following the chaining method pattern
	 */
	public MethodValidationConfiguration allowMultipleCascadedValidationOnReturnValues(boolean allow) {
		this.allowMultipleCascadedValidationOnReturnValues = allow;
		return this;
	}


	/**
	 * Define whether parallel methods that define constraints should throw a {@code ConstraintDefinitionException}. The
	 * default value is {@code false}, i.e. do not allow.
	 *
	 * See Section 4.5.5 of JSR-349 Specification, specifically
	 * "If a sub type overrides/implements a method originally defined in several parallel types of the hierarchy
	 * (e.g. two interfaces not extending each other, or a class and an interface not implemented by said class),
	 * no parameter constraints may be declared for that method at all nor parameters be marked for cascaded validation.
	 * This again is to avoid an unexpected strengthening of preconditions to be fulfilled by the caller."
	 *
	 * @param allow flag determining whether validation will allow parameter constraints in parallel hierarchies
	 *
	 * @return {@code this} following the chaining method pattern
	 */
	public MethodValidationConfiguration allowParallelMethodsDefineParameterConstraints(boolean allow) {
		this.allowParallelMethodsDefineParameterConstraints = allow;
		return this;
	}

	/**
	 * @return {@code true} if more than one return value within a class hierarchy can be marked for cascaded
	 * validation, {@code false} otherwise.
	 */
	public boolean isAllowOverridingMethodAlterParameterConstraint() {
		return this.allowOverridingMethodAlterParameterConstraint;
	}

	/**
	 * @return {@code true} if more than one return value within a class hierarchy can be marked for cascaded
	 * validation, {@code false} otherwise.
	 */
	public boolean isAllowMultipleCascadedValidationOnReturnValues() {
		return this.allowMultipleCascadedValidationOnReturnValues;
	}

	/**
	 * @return {@code true} if constraints on methods in parallel class hierarchy are allowed, {@code false} otherwise.
	 */
	public boolean isAllowParallelMethodsDefineParameterConstraints() {
		return this.allowParallelMethodsDefineParameterConstraints;
	}

	/**
	 * Return an unmodifiable Set of MethodConfigurationRule that are to be
	 * enforced based on the configuration.
	 *
	 * @return a set of method configuration rules based on this configuration state
	 */
	public Set<Class<? extends MethodConfigurationRule>> getConfiguredRuleSet() {
		HashSet<Class<? extends MethodConfigurationRule>> result = new HashSet<Class<? extends MethodConfigurationRule>>();

		if ( !this.isAllowOverridingMethodAlterParameterConstraint() ) {
			result.add( OverridingMethodMustNotAlterParameterConstraints.class );
		}

		if ( !this.isAllowParallelMethodsDefineParameterConstraints() ) {
			result.add( ParallelMethodsMustNotDefineParameterConstraints.class );
		}

		result.add( VoidMethodsMustNotBeReturnValueConstrained.class );

		if ( !this.isAllowMultipleCascadedValidationOnReturnValues() ) {
			result.add( ReturnValueMayOnlyBeMarkedOnceAsCascadedPerHierarchyLine.class );
		}

		result.add( ParallelMethodsMustNotDefineGroupConversionForCascadedReturnValue.class );

		return Collections.unmodifiableSet( result );
	}
}
