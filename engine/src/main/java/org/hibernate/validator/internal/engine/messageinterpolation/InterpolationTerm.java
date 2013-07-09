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
package org.hibernate.validator.internal.engine.messageinterpolation;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.PropertyNotFoundException;
import javax.el.ValueExpression;
import javax.validation.MessageInterpolator;

import org.hibernate.validator.internal.engine.MessageInterpolatorContext;
import org.hibernate.validator.internal.engine.messageinterpolation.el.RootResolver;
import org.hibernate.validator.internal.engine.messageinterpolation.el.SimpleELContext;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Helper class dealing with the interpolation of a single message parameter or expression extracted from a message
 * descriptor.
 *
 * @author Hardy Ferentschik
 */
public class InterpolationTerm {
	private static final Log log = LoggerFactory.make();

	/**
	 * Name under which the currently validate value is bound to the EL context.
	 */
	private static final String VALIDATED_VALUE_NAME = "validatedValue";

	/**
	 * Meta character to designate an EL expression.
	 */
	private static final String EL_DESIGNATION_CHARACTER = "$";

	/**
	 * Factory for creating EL expressions
	 */
	private static final ExpressionFactory expressionFactory;

	static {
		expressionFactory = ExpressionFactory.newInstance();
	}

	/**
	 * The actual expression (parameter or EL expression).
	 */
	private final String expression;

	/**
	 * The type of the expression.
	 */
	private final InterpolationTermType type;

	/**
	 * The locale for which to interpolate the expression.
	 */
	private final Locale locale;

	public InterpolationTerm(String expression, Locale locale) {
		this.locale = locale;
		this.expression = expression;
		if ( expression.startsWith( EL_DESIGNATION_CHARACTER ) ) {
			this.type = InterpolationTermType.EL;
		}
		else {
			this.type = InterpolationTermType.PARAMETER;
		}
	}

	public String interpolate(MessageInterpolator.Context context) {
		if ( InterpolationTermType.EL.equals( type ) ) {
			return interpolateExpressionLanguageTerm( context );
		}
		else {
			return interpolateConstraintAnnotationValue( context );
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "InterpolationExpression" );
		sb.append( "{expression='" ).append( expression ).append( '\'' );
		sb.append( ", type=" ).append( type );
		sb.append( '}' );
		return sb.toString();
	}

	private String interpolateExpressionLanguageTerm(MessageInterpolator.Context context) {
		String resolvedExpression = expression;
		SimpleELContext elContext = new SimpleELContext();
		try {
			ValueExpression valueExpression = bindContextValues( expression, context, elContext );
			resolvedExpression = (String) valueExpression.getValue( elContext );
		}
		catch ( PropertyNotFoundException pnfe ) {
			log.unknownPropertyInExpressionLanguage( expression, pnfe );
		}
		catch ( ELException e ) {
			log.errorInExpressionLanguage( expression, e );
		}
		catch ( Exception e ) {
			log.evaluatingExpressionLanguageExpressionCausedException( expression, e );
		}

		return resolvedExpression;
	}

	private String interpolateConstraintAnnotationValue(MessageInterpolator.Context context) {
		String resolvedExpression;
		Object variable = context.getConstraintDescriptor()
				.getAttributes()
				.get( removeCurlyBraces( expression ) );
		if ( variable != null ) {
			if ( variable.getClass().isArray() ) {
				resolvedExpression = Arrays.toString( (Object[]) variable );
			}
			else {
				resolvedExpression = variable.toString();
			}
		}
		else {
			resolvedExpression = expression;
		}
		return resolvedExpression;
	}

	private String removeCurlyBraces(String parameter) {
		return parameter.substring( 1, parameter.length() - 1 );
	}

	private ValueExpression bindContextValues(String messageTemplate, MessageInterpolator.Context messageInterpolatorContext, SimpleELContext elContext) {
		// bind the validated value
		ValueExpression valueExpression = expressionFactory.createValueExpression(
				messageInterpolatorContext.getValidatedValue(),
				Object.class
		);
		elContext.setVariable( VALIDATED_VALUE_NAME, valueExpression );

		// bind a formatter instantiated with proper locale
		valueExpression = expressionFactory.createValueExpression(
				new FormatterWrapper( locale ),
				FormatterWrapper.class
		);
		elContext.setVariable( RootResolver.FORMATTER, valueExpression );

		// map the annotation values
		for ( Map.Entry<String, Object> entry : messageInterpolatorContext.getConstraintDescriptor()
				.getAttributes()
				.entrySet() ) {
			valueExpression = expressionFactory.createValueExpression( entry.getValue(), Object.class );
			elContext.setVariable( entry.getKey(), valueExpression );
		}

		// check for custom parameters provided by HibernateConstraintValidatorContext
		if ( messageInterpolatorContext instanceof MessageInterpolatorContext ) {
			MessageInterpolatorContext internalContext = (MessageInterpolatorContext) messageInterpolatorContext;
			for ( Map.Entry<String, Object> entry : internalContext.getMessageParameters().entrySet() ) {
				valueExpression = expressionFactory.createValueExpression( entry.getValue(), Object.class );
				elContext.setVariable( entry.getKey(), valueExpression );
			}
		}

		return expressionFactory.createValueExpression( elContext, messageTemplate, String.class );
	}
}


