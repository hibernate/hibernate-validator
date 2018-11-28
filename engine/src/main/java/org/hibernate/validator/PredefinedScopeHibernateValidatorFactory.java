/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator;

import java.time.Duration;

import javax.validation.ValidatorFactory;

import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;
import org.hibernate.validator.spi.scripting.ScriptEvaluator;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

/**
 * Provides Hibernate Validator extensions to {@link ValidatorFactory} in the context of a predefined scope.
 *
 * @since 6.1
 */
@Incubating
public interface PredefinedScopeHibernateValidatorFactory extends ValidatorFactory {

	/**
	 * Returns the factory responsible for creating {@link ScriptEvaluator}s used to
	 * evaluate script expressions of {@link ScriptAssert} and {@link ParameterScriptAssert}
	 * constraints.
	 *
	 * @return a {@link ScriptEvaluatorFactory} instance
	 */
	@Incubating
	ScriptEvaluatorFactory getScriptEvaluatorFactory();

	/**
	 * Returns the temporal validation tolerance i.e. the acceptable margin of error when comparing date/time in
	 * temporal constraints.
	 *
	 * @return the tolerance
	 */
	@Incubating
	Duration getTemporalValidationTolerance();

	/**
	 * Returns the getter property selection strategy defining the rules determining if a method is a getter or not.
	 *
	 * @return the getter property selection strategy of the current {@link ValidatorFactory}
	 */
	@Incubating
	GetterPropertySelectionStrategy getGetterPropertySelectionStrategy();

	/**
	 * Returns a context for validator configuration via options from the
	 * Bean Validation API as well as specific ones from Hibernate Validator.
	 *
	 * @return A context for validator configuration.
	 */
	@Override
	HibernateValidatorContext usingContext();
}
