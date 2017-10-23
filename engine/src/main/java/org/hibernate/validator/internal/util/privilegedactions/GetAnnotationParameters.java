/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.util.Map;

import org.hibernate.validator.internal.util.annotationfactory.AnnotationParameters;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

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
			// HV-1184 Exclude synthetic methods potentially introduced by jacoco
			if ( m.isSynthetic() ) {
				continue;
			}

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
}
