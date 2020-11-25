/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation;

import java.lang.invoke.MethodHandles;
import java.util.Locale;
import java.util.Map;

import org.hibernate.validator.internal.engine.messageinterpolation.el.BeanMethodsELContext;
import org.hibernate.validator.internal.engine.messageinterpolation.el.BeanPropertiesElContext;
import org.hibernate.validator.internal.engine.messageinterpolation.el.DisabledFeatureELException;
import org.hibernate.validator.internal.engine.messageinterpolation.el.RootResolver;
import org.hibernate.validator.internal.engine.messageinterpolation.el.VariablesELContext;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.messageinterpolation.HibernateMessageInterpolatorContext;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.el.MethodNotFoundException;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.ValueExpression;
import jakarta.validation.MessageInterpolator;

/**
 * Resolver for the el expressions.
 *
 * @author Hardy Ferentschik
 * @author Adam Stawicki
 * @author Guillaume Smet
 */
public class ElTermResolver implements TermResolver {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

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
	private final ExpressionFactory expressionFactory;

	/**
	 * Construct the resolver. The expression factory has to be passed in to ensure that it is
	 * set up early and to allow for application control.
	 * @param locale the locale.
	 * @param expressionFactory the expression factory.
     */
	public ElTermResolver(Locale locale, ExpressionFactory expressionFactory) {
		this.locale = locale;
		this.expressionFactory = expressionFactory;
	}

	@Override
	public String interpolate(MessageInterpolator.Context context, String expression) {
		String resolvedExpression = expression;

		ELContext elContext = getElContext( context );

		try {
			ValueExpression valueExpression = bindContextValues( expression, context, elContext );
			resolvedExpression = (String) valueExpression.getValue( elContext );
		}
		catch (DisabledFeatureELException dfee) {
			LOG.disabledFeatureInExpressionLanguage( expression, dfee );
		}
		catch (PropertyNotFoundException pnfe) {
			LOG.unknownPropertyInExpressionLanguage( expression, pnfe );
		}
		catch (MethodNotFoundException mnfe) {
			LOG.unknownMethodInExpressionLanguage( expression, mnfe );
		}
		catch (ELException e) {
			LOG.errorInExpressionLanguage( expression, e );
		}
		catch (Exception e) {
			LOG.evaluatingExpressionLanguageExpressionCausedException( expression, e );
		}

		return resolvedExpression;
	}

	private ELContext getElContext(MessageInterpolator.Context context) {
		if ( !( context instanceof HibernateMessageInterpolatorContext ) ) {
			return new VariablesELContext( expressionFactory );
		}

		switch ( ( (HibernateMessageInterpolatorContext) context ).getExpressionLanguageFeatureLevel() ) {
			case NONE:
				throw LOG.expressionsNotResolvedWhenExpressionLanguageFeaturesDisabled();
			case VARIABLES:
				return new VariablesELContext( expressionFactory );
			case BEAN_PROPERTIES:
				return new BeanPropertiesElContext( expressionFactory );
			case BEAN_METHODS:
				return new BeanMethodsELContext( expressionFactory );
			default:
				throw LOG.expressionsLanguageFeatureLevelNotSupported();
		}
	}

	private ValueExpression bindContextValues(String messageTemplate, MessageInterpolator.Context messageInterpolatorContext, ELContext elContext) {
		// bind the validated value
		ValueExpression valueExpression = expressionFactory.createValueExpression(
				messageInterpolatorContext.getValidatedValue(),
				Object.class
		);
		elContext.getVariableMapper().setVariable( VALIDATED_VALUE_NAME, valueExpression );

		// bind a formatter instantiated with proper locale
		valueExpression = expressionFactory.createValueExpression(
				new FormatterWrapper( locale ),
				FormatterWrapper.class
		);
		elContext.getVariableMapper().setVariable( RootResolver.FORMATTER, valueExpression );

		// map the parameters provided by the annotation values and the parameters + expression variables explicitly
		// added to the context
		addVariablesToElContext( elContext, messageInterpolatorContext.getConstraintDescriptor().getAttributes() );
		if ( messageInterpolatorContext instanceof HibernateMessageInterpolatorContext ) {
			addVariablesToElContext( elContext, ( (HibernateMessageInterpolatorContext) messageInterpolatorContext ).getExpressionVariables() );
		}

		return expressionFactory.createValueExpression( elContext, messageTemplate, String.class );
	}

	private void addVariablesToElContext(ELContext elContext, Map<String, Object> variables) {
		for ( Map.Entry<String, Object> entry : variables.entrySet() ) {
			ValueExpression valueExpression = expressionFactory.createValueExpression( entry.getValue(), Object.class );
			elContext.getVariableMapper().setVariable( entry.getKey(), valueExpression );
		}
	}
}
