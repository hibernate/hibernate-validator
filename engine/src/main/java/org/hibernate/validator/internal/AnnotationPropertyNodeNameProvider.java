/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;

import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;

public class AnnotationPropertyNodeNameProvider implements PropertyNodeNameProvider, Serializable {
	private static final String VALUE = "value";

	private final Class<? extends Annotation> annotationType;
	private final String annotationMemberName;

	public AnnotationPropertyNodeNameProvider(Class<? extends Annotation> annotationType, String annotationMemberName) {
		Objects.requireNonNull( annotationType );

		this.annotationType = annotationType;
		this.annotationMemberName = annotationMemberName;
	}

	public AnnotationPropertyNodeNameProvider(Class<? extends Annotation> annotationType) {
		this( annotationType, VALUE );
	}

	@Override
	public String getName(String propertyName, Object object) {
		String resolvedName = propertyName;

		Field field = getField( propertyName, object );

		if ( field != null && field.isAnnotationPresent( annotationType ) ) {
			Annotation a = field.getAnnotation( annotationType );

			try {
				resolvedName = (String) a.annotationType().getMethod( annotationMemberName ).invoke( a );
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return resolvedName;
	}

	private Field getField(String fieldName, Object object) {
		for ( Field field : object.getClass().getFields() ) {
			field.setAccessible( true );
			String name = field.getName();

			if ( name.equals( fieldName ) ) {
				return field;
			}
		}

		return null;
	}
}
