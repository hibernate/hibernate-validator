/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.constraintvalidators;

import java.util.Map;
import javax.script.ScriptException;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.scriptengine.ScriptEvaluator;
import org.hibernate.validator.internal.util.scriptengine.ScriptEvaluatorFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

/**
 * Context used by validator implementations dealing with script expressions. Instances are thread-safe and can be re-used
 * several times to evaluate different bindings against one given given script expression.
 *
 * @author Gunnar Morling
 */
class ScriptAssertContext {

	private static final Log log = LoggerFactory.make();

	private final String script;
	private final ScriptEvaluator scriptEvaluator;

	public ScriptAssertContext(String languageName, String script) {
		this.script = script;
		this.scriptEvaluator = getScriptEvaluator( languageName );
	}

	public boolean evaluateScriptAssertExpression(Object object, String alias) {
		Map<String, Object> bindings = newHashMap();
		bindings.put( alias, object );

		return evaluateScriptAssertExpression( bindings );
	}

	public boolean evaluateScriptAssertExpression(Map<String, Object> bindings) {
		Object result;

		try {
			result = scriptEvaluator.evaluate( script, bindings );
		}
		catch ( ScriptException e ) {
			throw log.getErrorDuringScriptExecutionException( script, e );
		}

		return handleResult( result );
	}

	private ScriptEvaluator getScriptEvaluator(String languageName) {
		try {
			ScriptEvaluatorFactory evaluatorFactory = ScriptEvaluatorFactory.getInstance();
			return evaluatorFactory.getScriptEvaluatorByLanguageName( languageName );
		}
		catch ( ScriptException e ) {
			throw log.getCreationOfScriptExecutorFailedException( languageName, e );
		}
	}

	private boolean handleResult(Object evaluationResult) {
		if ( evaluationResult == null ) {
			throw log.getScriptMustReturnTrueOrFalseException( script );
		}

		if ( !( evaluationResult instanceof Boolean ) ) {
			throw log.getScriptMustReturnTrueOrFalseException(
					script,
					evaluationResult,
					evaluationResult.getClass().getCanonicalName()
			);
		}

		return Boolean.TRUE.equals( evaluationResult );
	}
}
