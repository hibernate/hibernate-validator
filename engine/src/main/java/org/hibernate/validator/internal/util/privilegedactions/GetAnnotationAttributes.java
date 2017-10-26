/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.util.Map;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Guillaume Smet
 */
public final class GetAnnotationAttributes implements PrivilegedAction<Map<String, Object>> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Annotation annotation;

	public static GetAnnotationAttributes action(Annotation annotation) {
		return new GetAnnotationAttributes( annotation );
	}

	private GetAnnotationAttributes(Annotation annotation) {
		this.annotation = annotation;
	}

	@Override
	public Map<String, Object> run() {
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
