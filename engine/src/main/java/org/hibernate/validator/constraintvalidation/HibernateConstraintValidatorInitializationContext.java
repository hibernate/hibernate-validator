/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.constraintvalidation;

import java.time.Clock;
import java.time.Duration;
import java.util.function.Supplier;

import jakarta.validation.ClockProvider;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.spi.scripting.ScriptEvaluator;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorNotFoundException;

/**
 * Provides contextual data and operations when initializing a constraint validator.
 *
 * @author Marko Bekhta
 * @since 6.0.5
 */
@Incubating
public interface HibernateConstraintValidatorInitializationContext {

	/**
	 * Returns a {@link ScriptEvaluator} created by the {@link ScriptEvaluatorFactory}
	 * passed at bootstrap.
	 *
	 * @param languageName the name of the scripting language
	 *
	 * @return a script evaluator for the given language. Never null.
	 *
	 * @throws ScriptEvaluatorNotFoundException in case no {@link ScriptEvaluator} was
	 * found for a given {@code languageName}
	 */
	ScriptEvaluator getScriptEvaluatorForLanguage(String languageName);

	/**
	 * Returns the provider for obtaining the current time in the form of a {@link Clock}, e.g. when validating the
	 * {@code Future} and {@code Past} constraints.
	 *
	 * @return the provider for obtaining the current time, never {@code null}. If no specific provider has been
	 * configured during bootstrap, a default implementation using the current system time and the current default time
	 * zone as returned by {@link Clock#systemDefaultZone()} will be returned.
	 */
	ClockProvider getClockProvider();

	/**
	 * Returns the temporal validation tolerance i.e. the acceptable margin of error when comparing date/time in
	 * temporal constraints.
	 *
	 * @return the tolerance
	 *
	 * @since 6.0.5
	 */
	@Incubating
	Duration getTemporalValidationTolerance();

	/**
	 * Returns an instance of the specified data type or {@code null} if the current context does not
	 * contain such data.
	 * The requested data type must match the one with which it was originally added with
	 * {@link org.hibernate.validator.HibernateValidatorConfiguration#addConstraintValidatorInitializationSharedData(Object)}.
	 *
	 * @param type the type of data to retrieve
	 * @return an instance of the specified type or {@code null} if the current constraint initialization context does not
	 * contain an instance of such type
	 *
	 * @since 9.1.0
	 * @see org.hibernate.validator.HibernateValidatorConfiguration#addConstraintValidatorInitializationSharedData(Object)
	 */
	@Incubating
	<C> C getSharedData(Class<C> type);

	/**
	 * Returns an instance of the specified data type or attempts to create it with a supplier, if the current context does not
	 * contain such data.
	 *
	 * @param type the type of data to retrieve
	 * @param createIfNotPresent the supplier to create an instance of shared data, if it is not already present in this context.
	 * @return an instance of the specified type or {@code null} if the current constraint initialization context does not
	 * contain an instance of such type
	 *
	 * @since 9.1.0
	 * @see org.hibernate.validator.HibernateValidatorConfiguration#addConstraintValidatorInitializationSharedData(Object)
	 */
	@Incubating
	<C, V extends C> C getSharedData(Class<C> type, Supplier<V> createIfNotPresent);
}
