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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class ValueMessageInterpolator implements MessageInterpolator {

	private static final String VALIDATED_VALUE_KEYWORD = "validatedValue";

	private static final String VALIDATED_VALUE_ALIAS = "_";

	private static final String VALIDATED_VALUE_SCRIPT_SEPARATOR = ":";

	private static final Pattern VALIDATED_VALUE_START_PATTERN = Pattern.compile( "\\$\\{" + VALIDATED_VALUE_KEYWORD );

	private final String scriptLang;

	private final MessageInterpolator delegate;

	/**
	 * Construct a default ValueMessageInterpolator
	 * who delegates initial interpolation to
	 * default ResourceBundleMessageInterpolator and use
	 * the given default script language.
	 *
	 * @param scriptLang the script language used in message interpolation
	 */
	public ValueMessageInterpolator(String scriptLang) {
		this( null, scriptLang );
	}

	/**
	 * Construct a ValueMessageInterpolator who delegates
	 * the initial interpolation to the given MessageInterpolator and
	 * use the default given script language used for toString interpolation.
	 *
	 * @param userMessageInterpolator the user specified message interpolator
	 * @param scriptLang the script language used in message interpolation
	 */
	public ValueMessageInterpolator(MessageInterpolator userMessageInterpolator, String scriptLang) {
		if ( userMessageInterpolator == null ) {
			//Use default ResourceBundleMessageInterpolator
			this.delegate = new ResourceBundleMessageInterpolator();
		}
		else {
			this.delegate = userMessageInterpolator;
		}

		this.scriptLang = scriptLang;
	}

	/**
	 * {@inheritDoc}
	 */
	public String interpolate(String message, Context context) {
		return interpolateMessage( delegate.interpolate( message, context ), context.getValidatedValue() );
	}

	/**
	 * {@inheritDoc}
	 */
	public String interpolate(String message, Context context, Locale locale) {
		return interpolateMessage( delegate.interpolate( message, context, locale ), context.getValidatedValue() );
	}

	/**
	 * Interpolate the validated value in the given message
	 *
	 * @param message the message where validated value have to be interpolated
	 * @param validatedValue the value of the object being validated
	 *
	 * @return the interpolated message
	 */
	private String interpolateMessage(String message, Object validatedValue) {
		String interpolatedMessage = message;
		Matcher matcher = VALIDATED_VALUE_START_PATTERN.matcher( message );

		while ( matcher.find() ) {
			int nbOpenCurlyBrace = 1;
			boolean isDoubleQuoteBloc = false;
			boolean isSimpleQuoteBloc = false;
			int lastIndex = matcher.end();

			do {

				char current = message.charAt( lastIndex );

				if ( current == '\'' ) {
					if ( !isDoubleQuoteBloc && !isEscaped( message, lastIndex ) ) {
						isSimpleQuoteBloc = !isSimpleQuoteBloc;
					}
				}
				else if ( current == '"' ) {
					if ( !isSimpleQuoteBloc && !isEscaped( message, lastIndex ) ) {
						isDoubleQuoteBloc = !isDoubleQuoteBloc;
					}
				}
				else if ( !isDoubleQuoteBloc && !isSimpleQuoteBloc ) {
					if ( current == '{' ) {
						nbOpenCurlyBrace++;
					}
					else if ( current == '}' ) {
						nbOpenCurlyBrace--;
					}
				}

				lastIndex++;

			} while ( nbOpenCurlyBrace > 0 && lastIndex < message.length() );

			//The validated value expression seems correct
			if ( nbOpenCurlyBrace == 0 ) {
				String expression = message.substring( matcher.start(), lastIndex );
				String expressionValue = evaluateValidatedValueExpr( expression, validatedValue );

				interpolatedMessage = interpolatedMessage.replaceFirst(
						Pattern.quote( expression ), Matcher.quoteReplacement( expressionValue )
				);
			}
		}

		return interpolatedMessage;
	}

	/**
	 * Returns the value of the validated value expression.
	 *
	 * @param expression the expression to interpolate
	 * @param validatedValue the value of the object being validated
	 *
	 * @return the interpolated value
	 */
	private String evaluateValidatedValueExpr(String expression, Object validatedValue) {
		String interpolatedValue;
		int separatorIndex = expression.indexOf( VALIDATED_VALUE_SCRIPT_SEPARATOR );

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

	/**
	 * Returns if the character at the given index in the String
	 * is an escaped character (preceded by a backslash character).
	 *
	 * @param enclosingString the string which contain the given character
	 * @param charIndex the index of the character
	 *
	 * @return true if the given character is escaped false otherwise
	 */
	private boolean isEscaped(String enclosingString, int charIndex) {
		if ( charIndex < 0 || charIndex > enclosingString.length() ) {
			throw new IndexOutOfBoundsException( "The given index must be between 0 and enclosingString.length() - 1" );
		}
		return charIndex > 0 && enclosingString.charAt( charIndex - 1 ) == '\\';
	}

}
