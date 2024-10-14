/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.actions;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Guillaume Smet
 */
public final class GetAnnotationAttributes {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private GetAnnotationAttributes() {
	}

	public static Map<String, Object> action(Annotation annotation) {
		final Method[] declaredMethods = annotation.annotationType().getDeclaredMethods();
		Map<String, Object> attributes = newHashMap( declaredMethods.length );

		for ( Method m : declaredMethods ) {
			// HV-1184 Exclude synthetic methods potentially introduced by jacoco
			if ( m.isSynthetic() ) {
				continue;
			}

			m.setAccessible( true );

			String attributeName = m.getName();

			try {
				attributes.put( m.getName(), m.invoke( annotation ) );
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw LOG.getUnableToGetAnnotationAttributeException( annotation.getClass(), attributeName, e );
			}
		}
		return CollectionHelper.toImmutableMap( attributes );
	}
}
