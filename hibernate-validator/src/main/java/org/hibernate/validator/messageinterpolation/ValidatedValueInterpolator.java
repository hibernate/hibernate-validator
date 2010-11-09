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
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.validation.MessageInterpolator;

import org.slf4j.Logger;

import org.hibernate.validator.util.LoggerFactory;

/**
 * Validated Value interpolator.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
//TODO use same system as the ScriptEvaluator used in @ScriptAssert
//TODO bug do an escape of meta character in all string ResourceBundleMessageinterpolator use Matcher.quoteReplacement
//TODO script with inner bracket
//TODO let the user to specify the language script
public class ValidatedValueInterpolator implements MessageInterpolator {

	public static final String VALIDATED_VALUE_KEYWORD = "validatedValue";

	public static final Pattern VALIDATED_VALUE_PATTERN = Pattern.compile( "\\$\\{" + VALIDATED_VALUE_KEYWORD + "(:[^\\}]+)?\\}" );

	private static final String VALIDATED_VALUE_ALIAS = "_";

	private final Logger logger = LoggerFactory.make();

	private final String scriptLang;

	private final MessageInterpolator delegate;

	/**
	 * Construct a default ValidatedValueInterpolator
	 * who delegates initial interpolation to
	 * default ResourceBundleMessageInterpolator and use
	 * the given default script language.
	 *
	 * @param scriptLang the script language used in message interpolation
	 */
	public ValidatedValueInterpolator(String scriptLang) {
		this( null, scriptLang );
	}

	/**
	 * Construct a ValidatedValueInterpolator who delegates
	 * the initial interpolation to the given MessageInterpolator and
	 * use the default given script language used for toString interpolation.
	 *
	 * @param userMessageInterpolator the user specified message interpolator
	 * @param scriptLang the script language used in message interpolation
	 */
	public ValidatedValueInterpolator(MessageInterpolator userMessageInterpolator, String scriptLang) {
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
	 * @param validatedValue the value under validation
	 *
	 * @return the interpolated message
	 */
	private String interpolateMessage(String message, Object validatedValue) {

		StringBuffer interpolatedMessage = new StringBuffer();
		Matcher matcher = VALIDATED_VALUE_PATTERN.matcher( message );

		while ( matcher.find() ) {
			String interpolatedToString;
			if ( matcher.groupCount() == 1 && matcher.group( 1 ) != null ) {
				String toStringScript = matcher.group( 1 ).substring( 1 );
				interpolatedToString = doToStringScriptEval(
						toStringScript, validatedValue, scriptLang
				);
			}
			else {
				interpolatedToString = String.valueOf( validatedValue );
			}
			matcher.appendReplacement( interpolatedMessage, Matcher.quoteReplacement( interpolatedToString ) );
		}
		matcher.appendTail( interpolatedMessage );

		return interpolatedMessage.toString();
	}

	/**
	 * Evaluate the toString script with JSR 223 and returns
	 * the string representation of the result. If
	 * the script evaluation fails the default toString
	 * method of the validated vaue is used.
	 *
	 * @param script the script to be evaluated
	 * @param validatedValue the value under validation
	 * @param scriptLang the script language
	 *
	 * @return the string result of the script evaluation
	 */
	private String doToStringScriptEval(String script, Object validatedValue, String scriptLang) {

		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName( scriptLang );

		if ( engine != null ) {
			engine.put( VALIDATED_VALUE_ALIAS, validatedValue );
			try {
				Object result = engine.eval( script );
				return String.valueOf( result );
			}
			catch ( ScriptException e ) {
				//Ignore - by default return the toString method results of the validatedValue
				logger.error( "Error during evaluation of script " + script, e );
			}
		}

		return String.valueOf( validatedValue );
	}

}
