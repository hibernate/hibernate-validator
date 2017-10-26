/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.scripting.ScriptEvaluator;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorNotFoundException;

/**
 * @author Marko Bekhta
 */
public abstract class AbstractScriptAssertValidator<A extends Annotation, T> implements ConstraintValidator<A, T> {

	private static final Log log = LoggerFactory.make( MethodHandles.lookup() );

	protected String languageName;
	protected String script;
	protected String escapedScript;
	protected volatile ScriptAssertContext scriptAssertContext;

	protected ScriptAssertContext getScriptAssertContext(ConstraintValidatorContext constraintValidatorContext) {
		if ( scriptAssertContext == null ) {
			synchronized ( this ) {
				if ( scriptAssertContext == null ) {
					ScriptEvaluator scriptEvaluator = null;
					if ( constraintValidatorContext instanceof HibernateConstraintValidatorContext ) {
						try {
							scriptEvaluator = constraintValidatorContext.unwrap( HibernateConstraintValidatorContext.class )
									.getScriptEvaluatorForLanguage( languageName );
						}
						catch (ScriptEvaluatorNotFoundException e) {
							throw log.getCreationOfScriptExecutorFailedException( languageName, e );
						}
					}
					scriptAssertContext = new ScriptAssertContext( script, scriptEvaluator );
				}
			}
		}
		return scriptAssertContext;
	}
}
