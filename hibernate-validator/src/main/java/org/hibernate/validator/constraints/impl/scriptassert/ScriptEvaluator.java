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
package org.hibernate.validator.constraints.impl.scriptassert;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.validation.ConstraintDeclarationException;

/**
 * A wrapper around JSR 223 {@link ScriptEngine}s. This class is thread-safe.
 *
 * @author Gunnar Morling
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
	 * @throws ConstraintDeclarationException In case of any errors during script execution or if the script
	 *                                        returned null or another type than Boolean.
	 */
	public boolean evaluate(String script, Object obj, String objectAlias) {

		if ( engineAllowsParallelAccessFromMultipleThreads() ) {
			return doEvaluate( script, obj, objectAlias );
		}
		else {
			synchronized ( engine ) {
				return doEvaluate( script, obj, objectAlias );
			}
		}
	}

	private boolean doEvaluate(String script, Object obj, String objectAlias) {

		engine.put( objectAlias, obj );

		Object evaluationResult;

		try {
			evaluationResult = engine.eval( script );
		}
		catch ( ScriptException e ) {
			throw new ConstraintDeclarationException(
					"Error during execution of script \"" + script + "\" occured.", e
			);
		}

		if ( evaluationResult == null ) {
			throw new ConstraintDeclarationException( "Script \"" + script + "\" returned null, but must return either true or false." );
		}

		if ( !( evaluationResult instanceof Boolean ) ) {
			throw new ConstraintDeclarationException(
					"Script \"" + script + "\" returned " + evaluationResult + " (of type " + evaluationResult.getClass()
							.getCanonicalName() + "), but must return either true or false."
			);
		}

		return Boolean.TRUE.equals( evaluationResult );
	}

	/**
	 * Checks, whether the given engine is thread-safe or not.
	 *
	 * @return True, if the given engine is thread-safe, false otherwise.
	 */
	private boolean engineAllowsParallelAccessFromMultipleThreads() {

		String threadingType = ( String ) engine.getFactory().getParameter( "THREADING" );

		return "THREAD-ISOLATED".equals( threadingType ) || "STATELESS".equals( threadingType );
	}
}
