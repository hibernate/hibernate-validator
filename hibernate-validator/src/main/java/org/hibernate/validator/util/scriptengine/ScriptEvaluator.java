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
package org.hibernate.validator.util.scriptengine;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * A wrapper around JSR 223 {@link ScriptEngine}s. This class is thread-safe.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
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
	 * Makes the given object available in then engine-scoped script context and executes the given script.
	 * The execution of the script happens either synchronized or unsynchronized, depending on the engine's
	 * threading abilities.
	 *
	 * @param script The script to be executed.
	 * @param obj The object to be put into the context.
	 * @param objectAlias The name under which the given object shall be put into the context.
	 *
	 * @return The script's result.
	 *
	 * @throws ScriptException In case of any errors during script execution.
	 */
	public Object evaluate(String script, Object obj, String objectAlias) throws ScriptException {
		if ( engineAllowsParallelAccessFromMultipleThreads() ) {
			return doEvaluate( script, obj, objectAlias );
		}
		else {
			synchronized ( engine ) {
				return doEvaluate( script, obj, objectAlias );
			}
		}
	}

	private Object doEvaluate(String script, Object obj, String objectAlias) throws ScriptException {
		engine.put( objectAlias, obj );
		return engine.eval( script );
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
