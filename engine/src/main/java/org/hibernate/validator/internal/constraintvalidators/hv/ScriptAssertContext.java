/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.scripting.ScriptEvaluationException;
import org.hibernate.validator.spi.scripting.ScriptEvaluator;

/**
 * Context used by validator implementations dealing with script expressions. Instances are thread-safe and can be re-used
 * several times to evaluate different bindings against one given given script expression.
 *
 * @author Gunnar Morling
 * @author Marko Bekhta
 */
class ScriptAssertContext {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final String script;
	private final ScriptEvaluator scriptEvaluator;

	public ScriptAssertContext(String script, ScriptEvaluator scriptEvaluator) {
		this.script = script;
		this.scriptEvaluator = scriptEvaluator;
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
		catch (ScriptEvaluationException e) {
			throw LOG.getErrorDuringScriptExecutionException( script, e );
		}

		return handleResult( result );
	}

	private boolean handleResult(Object evaluationResult) {
		if ( evaluationResult == null ) {
			throw LOG.getScriptMustReturnTrueOrFalseException( script );
		}

		if ( !( evaluationResult instanceof Boolean ) ) {
			throw LOG.getScriptMustReturnTrueOrFalseException(
					script,
					evaluationResult,
					evaluationResult.getClass().getCanonicalName()
			);
		}

		return Boolean.TRUE.equals( evaluationResult );
	}
}
