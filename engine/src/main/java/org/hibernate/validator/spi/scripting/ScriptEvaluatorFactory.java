/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.scripting;

import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.hibernate.validator.constraints.ScriptAssert;

/**
 * Factory used to initialize the {@link ScriptEvaluator}s required to evaluate script expressions defined in
 * {@link ScriptAssert} and {@link ParameterScriptAssert} constraints.
 *
 * @author Marko Bekhta
 * @since 6.0.3
 */
@Incubating
public interface ScriptEvaluatorFactory {

	/**
	 * Retrieves a script evaluator {@link ScriptEvaluator} for the given language.
	 *
	 * @param languageName the name of a scripting language
	 *
	 * @return a script executor for the given language. Never null.
	 *
	 * @throws ScriptEvaluatorNotFoundException in case no {@link ScriptEvaluator} was
	 * found for a given {@code languageName}.
	 */
	ScriptEvaluator getScriptEvaluatorByLanguageName(String languageName);


	/**
	 * Clear the state of the factory.
	 * <p>
	 * Called when the {@link ValidatorFactory} is closed.
	 */
	void clear();
}
