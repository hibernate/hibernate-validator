/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public final class GetAnnotationAttribute<T> implements PrivilegedAction<T> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Annotation annotation;
	private final String attributeName;
	private final Class<T> type;

	public static <T> GetAnnotationAttribute<T> action(Annotation annotation, String attributeName, Class<T> type) {
		return new GetAnnotationAttribute<T>( annotation, attributeName, type );
	}

	private GetAnnotationAttribute(Annotation annotation, String attributeName, Class<T> type) {
		this.annotation = annotation;
		this.attributeName = attributeName;
		this.type = type;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T run() {
		try {
			Method m = annotation.getClass().getMethod( attributeName );
			m.setAccessible( true );
			Object o = m.invoke( annotation );
			if ( type.isAssignableFrom( o.getClass() ) ) {
				return (T) o;
			}
			else {
				throw LOG.getWrongAnnotationAttributeTypeException( annotation.annotationType(), attributeName, type, o.getClass() );
			}
		}
		catch (NoSuchMethodException e) {
			throw LOG.getUnableToFindAnnotationAttributeException( annotation.annotationType(), attributeName, e );
		}
		catch (IllegalAccessException | InvocationTargetException e) {
			throw LOG.getUnableToGetAnnotationAttributeException( annotation.annotationType(), attributeName, e );
		}
	}
}
