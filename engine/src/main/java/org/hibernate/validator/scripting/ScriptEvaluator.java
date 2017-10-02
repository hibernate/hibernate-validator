/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.scripting;

import java.util.Map;

import javax.script.ScriptEngine;

import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.internal.util.scriptengine.ScriptEvaluatorImpl;

/**
 * Is used to evaluate script expressions for {@link ScriptAssert}
 * and {@link ParameterScriptAssert} constraints. A default implementation
 * {@link ScriptEvaluatorImpl} is a wrapper around JSR 223
 * {@link ScriptEngine}s. But it can also be any user specific implementation.
 *
 * @author Marko Bekhta
 * @since 6.1
 */
public interface ScriptEvaluator {

	/**
	 * Evaluates a {@code script} expression and returns a result of this evaluation.
	 *
	 * @param script A script to evaluate
	 * @param bindings The bindings to be used
	 *
	 * @return The result of script evaluation
	 *
	 * @throws ScriptEvaluationException in case of any errors during script evaluation
	 */
	Object evaluate(String script, Map<String, Object> bindings) throws ScriptEvaluationException;

}
