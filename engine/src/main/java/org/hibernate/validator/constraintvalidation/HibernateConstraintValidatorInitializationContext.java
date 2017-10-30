/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraintvalidation;

import java.time.Clock;
import java.time.Duration;

import javax.validation.ClockProvider;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.spi.scripting.ScriptEvaluator;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorNotFoundException;

/**
 * Provides contextual data and operations when initializing constraint validator.
 *
 * @author Marko Bekhta
 * @since 6.1
 */
@Incubating
public interface HibernateConstraintValidatorInitializationContext {

	/**
	 * Returns a {@link ScriptEvaluator} created based on the {@link ScriptEvaluatorFactory}
	 * passed at bootstrap.
	 *
	 * @param languageName the name of the scripting language
	 *
	 * @return a script executor for the given language. Never null.
	 *
	 * @throws ScriptEvaluatorNotFoundException in case no {@link ScriptEvaluator} was
	 * found for a given {@code languageName}
	 */

	ScriptEvaluator getScriptEvaluatorForLanguage(String languageName);

	/**
	 * Returns clock skew tolerance as {@link Duration} which is used to determine
	 * acceptable margin of error in milliseconds, which is allowed
	 * when comparing date/time in time related constraints.
	 *
	 * @return a tolerance as  {@link Duration}
	 */
	Duration getClockSkewTolerance();

	/**
	 * Returns the provider for obtaining the current time in the form of a {@link Clock},
	 * e.g. when validating the {@code Future} and {@code Past} constraints.
	 *
	 * @return the provider for obtaining the current time, never {@code null}. If no
	 * specific provider has been configured during bootstrap, a default implementation using
	 * the current system time and the current default time zone as returned by
	 * {@link Clock#systemDefaultZone()} will be returned.
	 */
	ClockProvider getClockProvider();
}
