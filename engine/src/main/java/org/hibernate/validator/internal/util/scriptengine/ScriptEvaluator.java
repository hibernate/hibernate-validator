/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.scriptengine;

import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * A wrapper around JSR 223 {@link ScriptEngine}s. This class is thread-safe.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class ScriptEvaluator {

	private final ScriptEngine engine;

	/**
	 * Creates a new script executor.
	 *
	 * @param engine The engine to be wrapped.
	 */
	public ScriptEvaluator(ScriptEngine engine) {
		this.engine = engine;
	}

	/**
	 * Executes the given script, using the given variable bindings. The execution of the script happens either synchronized or
	 * unsynchronized, depending on the engine's threading abilities.
	 *
	 * @param script The script to be executed.
	 * @param bindings The bindings to be used.
	 *
	 * @return The script's result.
	 *
	 * @throws ScriptException In case of any errors during script execution.
	 */
	public Object evaluate(String script, Map<String, Object> bindings) throws ScriptException {
		if ( engineAllowsParallelAccessFromMultipleThreads() ) {
			return doEvaluate( script, bindings );
		}
		else {
			synchronized ( engine ) {
				return doEvaluate( script, bindings );
			}
		}
	}

	private Object doEvaluate(String script, Map<String, Object> bindings) throws ScriptException {
		return engine.eval( script, new SimpleBindings( bindings ) );
	}

	/**
	 * Checks, whether the given engine is thread-safe or not.
	 *
	 * @return True, if the given engine is thread-safe, false otherwise.
	 */
	private boolean engineAllowsParallelAccessFromMultipleThreads() {
		String threadingType = (String) engine.getFactory().getParameter( "THREADING" );

		return "THREAD-ISOLATED".equals( threadingType ) || "STATELESS".equals( threadingType );
	}
}
