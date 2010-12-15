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

package org.hibernate.validator.messageinterpolation;

import javax.script.ScriptException;
import javax.validation.MessageInterpolator;
import javax.validation.ValidationException;

import org.hibernate.validator.util.scriptengine.ScriptEvaluator;
import org.hibernate.validator.util.scriptengine.ScriptEvaluatorFactory;

/**
 * Validated value message interpolator.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class ValueScriptingMessageInterpolator extends AbstractFormattingMessageInterpolator
		implements MessageInterpolator {
	private static final String VALIDATED_VALUE_ALIAS = "_";
	private final String scriptLang;

	/**
	 * Construct a default message interpolator
	 * which delegates initial interpolation to
	 * default ResourceBundleMessageInterpolator and use
	 * the given default script language.
	 *
	 * @param scriptLang the script language used in message interpolation
	 */
	public ValueScriptingMessageInterpolator(String scriptLang) {
		this( null, scriptLang );
	}

	/**
	 * Construct a message interpolator which delegates
	 * the initial interpolation to the given MessageInterpolator and
	 * use the default given script language used for toString interpolation.
	 *
	 * @param userMessageInterpolator the user specified message interpolator
	 * @param scriptLang the script language used in message interpolation
	 */
	public ValueScriptingMessageInterpolator(MessageInterpolator userMessageInterpolator, String scriptLang) {
		super( userMessageInterpolator );
		this.scriptLang = scriptLang;
	}

	/**
	 * Returns the value of the validated value expression.
	 *
	 * @param expression the expression to interpolate
	 * @param validatedValue the value of the object being validated
	 *
	 * @return the interpolated value
	 */
	String interpolateValidatedValue(String expression, Object validatedValue) {
		String interpolatedValue;
		int separatorIndex = expression.indexOf( VALIDATED_VALUE_FORMAT_SEPARATOR );

		if ( separatorIndex == -1 ) { //Use validated object toString method
			interpolatedValue = String.valueOf( validatedValue );
		}
		else { //Use evaluation of toString script
			String toStringScript = expression.substring( separatorIndex + 1, expression.length() - 1 );
			interpolatedValue = doToStringScriptEval( toStringScript, validatedValue, scriptLang );
		}

		return interpolatedValue;
	}

	/**
	 * Evaluate the toString script with JSR 223 and returns
	 * the String representation of the result.
	 *
	 * @param script the script to be evaluated
	 * @param validatedValue the value of the object being validated
	 * @param scriptLang the script language
	 *
	 * @return the string result of the script evaluation
	 *
	 * @throws ValidationException If no JSR-223 engine exists for the given script language
	 * or if any errors occur during the script execution
	 */
	private String doToStringScriptEval(String script, Object validatedValue, String scriptLang) {
		Object evaluationResult;
		ScriptEvaluator scriptEvaluator;

		try {
			ScriptEvaluatorFactory scriptEvaluatorFactory = ScriptEvaluatorFactory.getInstance();
			scriptEvaluator = scriptEvaluatorFactory.getScriptEvaluatorByLanguageName( scriptLang );
		}
		catch ( ScriptException e ) {
			throw new ValidationException( e );
		}

		try {
			evaluationResult = scriptEvaluator.evaluate( script, validatedValue, VALIDATED_VALUE_ALIAS );
		}
		catch ( ScriptException e ) {
			throw new ValidationException( "Error during execution of script \"" + script + "\" occured.", e );
		}

		if ( evaluationResult == null ) {
			throw new ValidationException( "Script \"" + script + "\" returned null, but must return a string representation of the object being validated." );
		}

		if ( !( evaluationResult instanceof String ) ) {
			throw new ValidationException(
					"Script \"" + script + "\" returned a result of type " + evaluationResult.getClass()
							.getCanonicalName() + " but must return a string representation of the object being validated."
			);
		}

		return String.valueOf( evaluationResult );
	}
}
