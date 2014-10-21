/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public final class GetAnnotationParameter<T> implements PrivilegedAction<T> {

	private static final Log log = LoggerFactory.make();

	private final Annotation annotation;
	private final String parameterName;
	private final Class<T> type;

	public static <T> GetAnnotationParameter<T> action(Annotation annotation, String parameterName, Class<T> type) {
		return new GetAnnotationParameter<T>( annotation, parameterName, type );
	}

	private GetAnnotationParameter(Annotation annotation, String parameterName, Class<T> type) {
		this.annotation = annotation;
		this.parameterName = parameterName;
		this.type = type;
	}

	@Override
	public T run() {
		try {
			Method m = annotation.getClass().getMethod( parameterName );
			m.setAccessible( true );
			Object o = m.invoke( annotation );
			if ( type.isAssignableFrom( o.getClass() ) ) {
				return (T) o;
			}
			else {
				throw log.getWrongParameterTypeException( type.getName(), o.getClass().getName() );
			}
		}
		catch ( NoSuchMethodException e ) {
			throw log.getUnableToFindAnnotationParameterException( parameterName, e );
		}
		catch ( IllegalAccessException e ) {
			throw log.getUnableToGetAnnotationParameterException( parameterName, annotation.getClass().getName(), e );
		}
		catch ( InvocationTargetException e ) {
			throw log.getUnableToGetAnnotationParameterException( parameterName, annotation.getClass().getName(), e );
		}
	}
}
