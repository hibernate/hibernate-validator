/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation;

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
 * Resolver for the el expressions.
 * 
 * @author Hardy Ferentschik
 * @author Adam Stawicki
 */
public class ElTermResolver implements TermResolver {
	private static final Log log = LoggerFactory.make();

	/**
	 * Name under which the currently validate value is bound to the EL context.
	 */
	private static final String VALIDATED_VALUE_NAME = "validatedValue";

	/**
	 * The locale for which to interpolate the expression.
	 */
	private final Locale locale;

	/**
	 * Factory for creating EL expressions
	 */
	private static final ExpressionFactory expressionFactory;

	static {
		expressionFactory = ExpressionFactory.newInstance();
	}
	
	public ElTermResolver(Locale locale) {
		this.locale = locale;
	}

	@Override
	public String interpolate(MessageInterpolator.Context context, String expression) {
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
