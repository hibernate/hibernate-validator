/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.util.Map;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetAnnotationParameters.AnnotationParameters;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * @author Guillaume Smet
 */
public final class GetAnnotationParameters implements PrivilegedAction<AnnotationParameters> {

	private static final Log LOG = LoggerFactory.make();

	private final Annotation annotation;

	public static GetAnnotationParameters action(Annotation annotation) {
		return new GetAnnotationParameters( annotation );
	}

	private GetAnnotationParameters(Annotation annotation) {
		this.annotation = annotation;
	}

	@Override
	public AnnotationParameters run() {
		final Method[] declaredMethods = annotation.annotationType().getDeclaredMethods();
		Map<String, Object> parameters = newHashMap( declaredMethods.length );

		for ( Method m : declaredMethods ) {
			m.setAccessible( true );

			String parameterName = m.getName();

			try {
				parameters.put( m.getName(), m.invoke( annotation ) );
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw LOG.getUnableToGetAnnotationParameterException( parameterName, annotation.getClass(), e );
			}
		}
		return new AnnotationParameters( parameters );
	}

	public static class AnnotationParameters implements Serializable {

		@Immutable
		private final Map<String, Object> parameters;

		private AnnotationParameters(Map<String, Object> parameters) {
			this.parameters = CollectionHelper.toImmutableMap( parameters );
		}

		public Map<String, Object> getParameters() {
			return parameters;
		}

		@SuppressWarnings("unchecked")
		public <T> T getMandatoryParameter(String parameterName, Class<T> type) {
			Object parameter = parameters.get( parameterName );

			if ( parameter == null ) {
				throw LOG.getUnableToFindAnnotationParameterException( parameterName, null );
			}

			if ( !type.isAssignableFrom( parameter.getClass() ) ) {
				throw LOG.getWrongParameterTypeException( type, parameter.getClass() );
			}

			return (T) parameter;
		}

		@SuppressWarnings("unchecked")
		public <T> T getParameter(String parameterName, Class<T> type) {
			Object parameter = parameters.get( parameterName );

			if ( parameter == null ) {
				return null;
			}

			if ( !type.isAssignableFrom( parameter.getClass() ) ) {
				throw LOG.getWrongParameterTypeException( type, parameter.getClass() );
			}

			return (T) parameter;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append( this.getClass().getSimpleName() );
			sb.append( '{' );
			sb.append( "parameters=" ).append( parameters );
			sb.append( '}' );
			return sb.toString();
		}
	}
}
