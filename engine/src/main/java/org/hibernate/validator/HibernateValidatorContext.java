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
	 * @param timeProvider
	 *            the time provider to register.
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @hv.experimental This API is considered experimental and may change in future revisions
	 * @since 5.2
	 */
	HibernateValidatorContext timeProvider(TimeProvider timeProvider);
}
