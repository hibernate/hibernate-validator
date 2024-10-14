/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.actions;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public final class GetAnnotationAttribute {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private GetAnnotationAttribute() {
	}

	@SuppressWarnings("unchecked")
	public static <T> T action(Annotation annotation, String attributeName, Class<T> type) {
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
