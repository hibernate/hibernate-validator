/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.scripting.ScriptEvaluator;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorNotFoundException;

/**
 * @author Marko Bekhta
 */
public abstract class AbstractScriptAssertValidator<A extends Annotation, T> implements HibernateConstraintValidator<A, T> {

	private static final Log log = LoggerFactory.make( MethodHandles.lookup() );

	protected String languageName;
	protected String script;
	protected String escapedScript;
	protected volatile ScriptAssertContext scriptAssertContext;

	protected void initializeScriptContext(HibernateConstraintValidatorInitializationContext initializationContext) {
		try {
			ScriptEvaluator scriptEvaluator = initializationContext.getScriptEvaluatorForLanguage( languageName );
			scriptAssertContext = new ScriptAssertContext( script, scriptEvaluator );
		}
		catch (ScriptEvaluatorNotFoundException e) {
			throw log.getCreationOfScriptExecutorFailedException( languageName, e );
		}
	}
}
