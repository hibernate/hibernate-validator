/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation.el;

import java.beans.FeatureDescriptor;
import java.util.IllegalFormatException;
import java.util.Iterator;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.PropertyNotWritableException;

import org.hibernate.validator.internal.engine.messageinterpolation.FormatterWrapper;

/**
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class RootResolver extends ELResolver {
	/**
	 * Name under which to bind a formatter to the EL context.
	 */
	public static final String FORMATTER = "formatter";
	private static final String FORMAT = "format";

	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		return null;
	}

	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
		return null;
	}

	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		return null;
	}

	@Override
	public Object getValue(ELContext context, Object base, Object property) {
		return null;
	}

	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		return true;
	}

	@Override
	public void setValue(ELContext context, Object base, Object property, Object value) {
		throw new PropertyNotWritableException();
	}

	@Override
	public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
		if ( !( base instanceof FormatterWrapper ) ) {
			return null;
		}

		// due to bugs in most EL implementations when it comes to evaluating varargs we take care of the formatter call
		// ourselves.
		return evaluateFormatExpression( context, method, params );
	}

	private Object evaluateFormatExpression(ELContext context, Object method, Object[] params) {
		if ( !FORMAT.equals( method ) ) {
			throw new ELException( "Wrong method name 'formatter#" + method + "' does not exist. Only formatter#format is supported." );
		}

		if ( params.length == 0 ) {
			throw new ELException( "Invalid number of arguments to Formatter#format" );
		}

		if ( !( params[0] instanceof String ) ) {
			throw new ELException( "The first argument to Formatter#format must be String" );
		}

		FormatterWrapper formatterWrapper = (FormatterWrapper) context.getVariableMapper()
				.resolveVariable( FORMATTER )
				.getValue( context );
		Object[] formattingParameters = new Object[params.length - 1];
		System.arraycopy( params, 1, formattingParameters, 0, params.length - 1 );

		Object returnValue;
		try {
			returnValue = formatterWrapper.format( (String) params[0], formattingParameters );
			context.setPropertyResolved( true );
		}
		catch (IllegalFormatException e) {
			throw new ELException( "Error in Formatter#format call", e );
		}

		return returnValue;
	}

}
