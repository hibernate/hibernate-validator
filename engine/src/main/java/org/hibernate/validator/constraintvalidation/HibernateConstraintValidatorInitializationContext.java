/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraintvalidation;

import java.time.Clock;
import java.time.Duration;

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
}
