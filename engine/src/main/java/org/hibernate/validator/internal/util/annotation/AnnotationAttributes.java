/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.annotation;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

public class AnnotationAttributes implements Serializable {

	private static final Log LOG = LoggerFactory.make();

	@Immutable
	private final Map<String, Object> parameters;

	public AnnotationAttributes(Map<String, Object> parameters) {
		this.parameters = CollectionHelper.toImmutableMap( parameters );
	}

	public Map<String, Object> toMap() {
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

	public Object getParameter(String parameterName) {
		return parameters.get( parameterName );
	}

	public int size() {
		return parameters.size();
	}

	public Set<Map.Entry<String, Object>> entrySet() {
		return parameters.entrySet();
	}

	public Set<String> keySet() {
		return parameters.keySet();
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
