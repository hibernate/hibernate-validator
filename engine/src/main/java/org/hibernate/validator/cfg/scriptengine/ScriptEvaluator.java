/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.scriptengine;

import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Is used to evaluate script expressions for {@link org.hibernate.validator.constraints.ScriptAssert}
 * and {@link org.hibernate.validator.constraints.ParameterScriptAssert} constraints. A default implementation
 * {@link org.hibernate.validator.internal.util.scriptengine.ScriptEvaluatorImpl} is a wrapper around JSR 223
 * {@link ScriptEngine}s. But it can also be any user specific implementation.
 *
 * @author Marko Bekhta
 */
public interface ScriptEvaluator {

	/**
	 * Evaluates a {@code script} expression and returns a result of this evaluation.
	 *
	 * @param script A script to evaluate
	 * @param bindings The bindings to be used
	 * @return The result of script evaluation
	 *
	 * @throws ScriptException in case of any errors during script evaluation
	 */
	Object evaluate(String script, Map<String, Object> bindings) throws ScriptException;

}
