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

import java.util.Locale;
import javax.script.ScriptException;
import javax.validation.MessageInterpolator;
import javax.validation.ValidationException;

import org.hibernate.validator.util.scriptengine.ScriptEvaluator;
import org.hibernate.validator.util.scriptengine.ScriptEvaluatorFactory;

/**
 * A message interpolator which can interpolate the validated value and format this value using the Java Scripting
 * API as defined by <a href="http://jcp.org/en/jsr/detail?id=223">JSR 223</a>
 * ("Scripting for the Java<sup>TM</sup> Platform"). Therefore an
 * implementation of that API must part of the class path. This is automatically
 * the case when running on Java 6. For older Java versions, the JSR 223 RI can
 * be added manually to the class path.
 * <p>
 *  If no formatting script is specified {@code String.valueOf(validatedValue)} is called.
 * </p>
 * <p>
 * To interpolate the validated Value add {@code $&#123;validatedValue&#125;} into the message template. To specify
 * a format script use {@code $&#123;validatedValue:[script]&#125;}, eg {@code $&#123;validatedValue:_.toString()&#125;}.
 * The {@code _} is used to reference the validated value.
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

	String interpolateValidatedValue(String expression, Object validatedValue, Locale locale) {
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
