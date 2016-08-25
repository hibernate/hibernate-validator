/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.ValidatorContext;

import org.hibernate.validator.spi.time.TimeProvider;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

/**
 * Represents a Hibernate Validator specific context that is used to create
 * {@link javax.validation.Validator} instances. Adds additional configuration options to those
 * provided by {@link ValidatorContext}.
 *
 * @author Emmanuel Bernard
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Gunnar Morling
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
 */
public interface HibernateValidatorContext extends ValidatorContext {

	@Override
	HibernateValidatorContext messageInterpolator(MessageInterpolator messageInterpolator);

	@Override
	HibernateValidatorContext traversableResolver(TraversableResolver traversableResolver);

	@Override
	HibernateValidatorContext constraintValidatorFactory(ConstraintValidatorFactory factory);

	/**
	 * @since 5.2
	 */
	@Override
	HibernateValidatorContext parameterNameProvider(ParameterNameProvider parameterNameProvider);

	/**
	 * En- or disables the fail fast mode. When fail fast is enabled the validation
	 * will stop on the first constraint violation detected.
	 *
	 * @param failFast {@code true} to enable fail fast, {@code false} otherwise.
	 *
	 * @return {@code this} following the chaining method pattern
	 */
	HibernateValidatorContext failFast(boolean failFast);

	/**
	 * Registers the given validated value unwrapper with the bootstrapped validator. When validating an element which
	 * is of a type supported by the unwrapper and which is annotated with
	 * {@link org.hibernate.validator.valuehandling.UnwrapValidatedValue}, the unwrapper will be applied to retrieve the
	 * value to validate.
	 *
	 * @param handler the unwrapper to register
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @hv.experimental This API is considered experimental and may change in future revisions
	 */
	HibernateValidatorContext addValidationValueHandler(ValidatedValueUnwrapper<?> handler);

	/**
	 * Registers the given time provider with the bootstrapped validator. This provider will be used to obtain the
	 * current time when validating {@code @Future} and {@code @Past} constraints. If not set or if {@code null} is
	 * passed as a parameter, the time provider of the {@link javax.validation.ValidatorFactory} is used.
	 *
	 * @param timeProvider the time provider to register.
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @hv.experimental This API is considered experimental and may change in future revisions
	 * @since 5.2
	 */
	HibernateValidatorContext timeProvider(TimeProvider timeProvider);

	/**
	 * Define whether overriding methods that override constraints should throw a {@code ConstraintDefinitionException}.
	 * The default value is {@code false}, i.e. do not allow.
	 * <p>
	 * See Section 4.5.5 of the JSR 380 specification, specifically
	 * <pre>
	 * "In sub types (be it sub classes/interfaces or interface implementations), no parameter constraints may
	 * be declared on overridden or implemented methods, nor may parameters be marked for cascaded validation.
	 * This would pose a strengthening of preconditions to be fulfilled by the caller."
	 * </pre>
	 *
	 * @param allow flag determining whether validation will allow overriding to alter parameter constraints.
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 5.3
	 */
	HibernateValidatorContext allowOverridingMethodAlterParameterConstraint(boolean allow);

	/**
	 * Define whether more than one constraint on a return value may be marked for cascading validation are allowed.
	 * The default value is {@code false}, i.e. do not allow.
	 * <p>
	 * See Section 4.5.5 of the JSR 380 specification, specifically
	 * <pre>
	 * "One must not mark a method return value for cascaded validation more than once in a line of a class hierarchy.
	 * In other words, overriding methods on sub types (be it sub classes/interfaces or interface implementations)
	 * cannot mark the return value for cascaded validation if the return value has already been marked on the
	 * overridden method of the super type or interface."
	 * </pre>
	 *
	 * @param allow flag determining whether validation will allow multiple cascaded validation on return values.
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 5.3
	 */
	HibernateValidatorContext allowMultipleCascadedValidationOnReturnValues(boolean allow);

	/**
	 * Define whether parallel methods that define constraints should throw a {@code ConstraintDefinitionException}. The
	 * default value is {@code false}, i.e. do not allow.
	 * <p>
	 * See Section 4.5.5 of the JSR 380 specification, specifically
	 * <pre>
	 * "If a sub type overrides/implements a method originally defined in several parallel types of the hierarchy
	 * (e.g. two interfaces not extending each other, or a class and an interface not implemented by said class),
	 * no parameter constraints may be declared for that method at all nor parameters be marked for cascaded validation.
	 * This again is to avoid an unexpected strengthening of preconditions to be fulfilled by the caller."
	 * </pre>
	 *
	 * @param allow flag determining whether validation will allow parameter constraints in parallel hierarchies
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 5.3
	 */
	HibernateValidatorContext allowParallelMethodsDefineParameterConstraints(boolean allow);
}
