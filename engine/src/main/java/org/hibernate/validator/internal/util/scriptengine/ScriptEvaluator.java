/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,  
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
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
