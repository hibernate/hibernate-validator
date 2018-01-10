/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.scripting;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.SimpleBindings;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * A wrapper around JSR 223 {@link ScriptEngine}s. This class is thread-safe.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @since 6.0.3
 */
@Incubating
public class ScriptEngineScriptEvaluator implements ScriptEvaluator {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final ScriptEngine engine;

	/**
	 * Creates a new script executor.
	 *
	 * @param engine the engine to be wrapped
	 */
	public ScriptEngineScriptEvaluator(ScriptEngine engine) {
		this.engine = engine;
	}

	/**
	 * Executes the given script, using the given variable bindings. The execution of the script happens either synchronized or
	 * unsynchronized, depending on the engine's threading abilities.
	 *
	 * @param script the script to be executed
	 * @param bindings the bindings to be used
	 *
	 * @return the script's result
	 *
	 * @throws ScriptEvaluationException in case an error occurred during the script evaluation
	 */
	@Override
	public Object evaluate(String script, Map<String, Object> bindings) throws ScriptEvaluationException {
		if ( engineAllowsParallelAccessFromMultipleThreads() ) {
			return doEvaluate( script, bindings );
		}
		else {
			synchronized ( engine ) {
				return doEvaluate( script, bindings );
			}
		}
	}

	private Object doEvaluate(String script, Map<String, Object> bindings) throws ScriptEvaluationException {
		try {
			return engine.eval( script, new SimpleBindings( bindings ) );
		}
		catch (Exception e) {
			throw LOG.getErrorExecutingScriptException( script, e );
		}
	}

	/**
	 * Checks whether the given engine is thread-safe or not.
	 *
	 * @return true if the given engine is thread-safe, false otherwise.
	 */
	private boolean engineAllowsParallelAccessFromMultipleThreads() {
		String threadingType = (String) engine.getFactory().getParameter( "THREADING" );

		return "THREAD-ISOLATED".equals( threadingType ) || "STATELESS".equals( threadingType );
	}
}
