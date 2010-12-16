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
package org.hibernate.validator.constraints.impl;

import javax.script.ScriptException;
import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.util.scriptengine.ScriptEvaluator;
import org.hibernate.validator.util.scriptengine.ScriptEvaluatorFactory;

/**
 * Validator for the {@link ScriptAssert} constraint annotation.
 *
 * @author Gunnar Morling.
 * @author Hardy Ferentschik
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class ScriptAssertValidator implements ConstraintValidator<ScriptAssert, Object> {
	private String script;
	private String languageName;
	private String alias;

	public void initialize(ScriptAssert constraintAnnotation) {
		validateParameters( constraintAnnotation );

		this.script = constraintAnnotation.script();
		this.languageName = constraintAnnotation.lang();
		this.alias = constraintAnnotation.alias();
	}

	public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {

		Object evaluationResult;
		ScriptEvaluator scriptEvaluator;

		try {
			ScriptEvaluatorFactory evaluatorFactory = ScriptEvaluatorFactory.getInstance();
			scriptEvaluator = evaluatorFactory.getScriptEvaluatorByLanguageName( languageName );
		}
		catch ( ScriptException e ) {
			throw new ConstraintDeclarationException( e );
		}

		try {
			evaluationResult = scriptEvaluator.evaluate( script, value, alias );
		}
		catch ( ScriptException e ) {
			throw new ConstraintDeclarationException(
					"Error during execution of script \"" + script + "\" occurred.", e
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

	private void validateParameters(ScriptAssert constraintAnnotation) {
		if ( constraintAnnotation.script().length() == 0 ) {
			throw new IllegalArgumentException( "The parameter \"script\" must not be empty." );
		}
		if ( constraintAnnotation.lang().length() == 0 ) {
			throw new IllegalArgumentException( "The parameter \"lang\" must not be empty." );
		}
		if ( constraintAnnotation.alias().length() == 0 ) {
			throw new IllegalArgumentException( "The parameter \"alias\" must not be empty." );
		}
	}
}
