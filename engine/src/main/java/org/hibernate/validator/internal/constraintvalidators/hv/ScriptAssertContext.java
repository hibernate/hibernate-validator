/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import java.util.Map;

import javax.script.ScriptException;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.cfg.scriptengine.ScriptEvaluator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Context used by validator implementations dealing with script expressions. Instances are thread-safe and can be re-used
 * several times to evaluate different bindings against one given given script expression.
 *
 * @author Gunnar Morling
 */
class ScriptAssertContext {

	private static final Log log = LoggerFactory.make();

	private final String script;
	private final String languageName;

	public ScriptAssertContext(String languageName, String script) {
		this.script = script;
		this.languageName = languageName;
	}

	public boolean evaluateScriptAssertExpression(Object object, String alias, ConstraintValidatorContext context) {
		Map<String, Object> bindings = newHashMap();
		bindings.put( alias, object );

		return evaluateScriptAssertExpression( bindings, context );
	}

	public boolean evaluateScriptAssertExpression(Map<String, Object> bindings, ConstraintValidatorContext context) {
		Object result;

		try {
			// TODO: should ScriptEvaluator be cached ?
			result = getScriptEvaluator( context ).evaluate( script, bindings );
		}
		catch (ScriptException e) {
			throw log.getErrorDuringScriptExecutionException( script, e );
		}

		return handleResult( result );
	}

	private ScriptEvaluator getScriptEvaluator( ConstraintValidatorContext context) {
		try {
			return context.unwrap( HibernateConstraintValidatorContext.class ).getScriptEvaluatorForLanguage( languageName );
		}
		catch (ScriptException e) {
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
