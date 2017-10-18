/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.scripting;

import java.util.Map;

import javax.script.ScriptEngine;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.hibernate.validator.constraints.ScriptAssert;

/**
 * Used to evaluate script expressions for {@link ScriptAssert}
 * and {@link ParameterScriptAssert} constraints.
 * <p>
 * The default implementation {@link ScriptEngineScriptEvaluator} is a wrapper around JSR 223
 * {@link ScriptEngine}s. It can also be any user specific implementation.
 *
 * @author Marko Bekhta
 * @since 6.0.3
 */
@Incubating
public interface ScriptEvaluator {

	/**
	 * Evaluates a {@code script} expression and returns the result of this evaluation.
	 *
	 * @param script a script to evaluate
	 * @param bindings the bindings to be used
	 *
	 * @return the result of script evaluation
	 *
	 * @throws ScriptEvaluationException in case an error occurred during the script evaluation
	 */
	Object evaluate(String script, Map<String, Object> bindings) throws ScriptEvaluationException;
}
