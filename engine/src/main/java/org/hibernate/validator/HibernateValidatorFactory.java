/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator;

import java.time.Duration;

import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;
import org.hibernate.validator.spi.scripting.ScriptEvaluator;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

/**
 * Provides Hibernate Validator extensions to {@link ValidatorFactory}.
 *
 * @author Emmanuel Bernard
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public interface HibernateValidatorFactory extends ValidatorFactory {

	/**
	 * Returns the factory responsible for creating {@link ScriptEvaluator}s used to
	 * evaluate script expressions of {@link ScriptAssert} and {@link ParameterScriptAssert}
	 * constraints.
	 *
	 * @return a {@link ScriptEvaluatorFactory} instance
	 *
	 * @since 6.0.3
	 */
	@Incubating
	ScriptEvaluatorFactory getScriptEvaluatorFactory();

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
	 * Returns the getter property selection strategy defining the rules determining if a method is a getter or not.
	 *
	 * @return the getter property selection strategy of the current {@link ValidatorFactory}
	 *
	 * @since 6.1.0
	 */
	@Incubating
	GetterPropertySelectionStrategy getGetterPropertySelectionStrategy();

	/**
	 * Returns the property node name provider used to resolve the name of a property node when creating the property path.
	 *
	 * @return the property node name provider of the current {@link ValidatorFactory}
	 *
	 * @since 6.2.1
	 */
	@Incubating
	PropertyNodeNameProvider getPropertyNodeNameProvider();

	/**
	 * Returns a context for validator configuration via options from the
	 * Bean Validation API as well as specific ones from Hibernate Validator.
	 *
	 * @return A context for validator configuration.
	 */
	@Override
	HibernateValidatorContext usingContext();
}
