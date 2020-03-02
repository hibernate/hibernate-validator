/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator;

import java.time.Duration;

import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.TraversableResolver;
import jakarta.validation.ValidatorContext;
import jakarta.validation.valueextraction.ValueExtractor;

/**
 * Represents a Hibernate Validator specific context that is used to create
 * {@link jakarta.validation.Validator} instances. Adds additional configuration options to those
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
	 * @since 6.0
	 */
	@Override
	HibernateValidatorContext clockProvider(ClockProvider clockProvider);

	/**
	 * @since 6.0
	 */
	@Override
	HibernateValidatorContext addValueExtractor(ValueExtractor<?> extractor);

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

	/**
	 * Define whether the per validation call caching of {@link TraversableResolver} results is enabled. The default
	 * value is {@code true}, i.e. the caching is enabled.
	 * <p>
	 * This behavior was initially introduced to cache the {@code JPATraversableResolver} results but the map lookups it
	 * introduces can be counterproductive when the {@code TraversableResolver} calls are very fast.
	 *
	 * @param enabled flag determining whether per validation call caching is enabled for {@code TraversableResolver}
	 * results.
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 6.0.3
	 */
	HibernateValidatorContext enableTraversableResolverResultCache(boolean enabled);

	/**
	 * Define the temporal validation tolerance i.e. the acceptable margin of error
	 * when comparing date/time in temporal constraints.
	 *
	 * @param temporalValidationTolerance the tolerance
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 6.0.5
	 */
	@Incubating
	HibernateValidatorContext temporalValidationTolerance(Duration temporalValidationTolerance);

	/**
	 * Define a payload passed to the constraint validators. If the method is called multiple times, only the payload
	 * passed last will be propagated.
	 *
	 * @param constraintValidatorPayload the payload passed to constraint validators
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 6.0.8
	 */
	@Incubating
	HibernateValidatorContext constraintValidatorPayload(Object constraintValidatorPayload);
}
