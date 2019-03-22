/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.spi.nodenameprovider;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;

import org.hibernate.validator.spi.nodenameprovider.JavaBeanProperty;
import org.hibernate.validator.spi.nodenameprovider.Property;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;

/**
 * An example of how a name can be resolved from an annotation
 *
 * @author Damir Alibegovic
 */
class AnnotationPropertyNodeNameProvider implements PropertyNodeNameProvider, Serializable {
	private static final String VALUE = "value";

	private final Class<? extends Annotation> annotationType;
	private final String annotationMemberName;

	private AnnotationPropertyNodeNameProvider(Class<? extends Annotation> annotationType, String annotationMemberName) {
		Objects.requireNonNull( annotationType );

		this.annotationType = annotationType;
		this.annotationMemberName = annotationMemberName;
	}

	AnnotationPropertyNodeNameProvider(Class<? extends Annotation> annotationType) {
		this( annotationType, VALUE );
	}

	@Override
	public String getName(Property property) {
		if ( property instanceof JavaBeanProperty ) {
			return getJavaBeanPropertyName( (JavaBeanProperty) property );
		}

		return getDefaultName( property );
	}

	private String getJavaBeanPropertyName(JavaBeanProperty property) {
		String resolvedName = property.getName();

		Field field = getField( resolvedName, property.getDeclaringClass() );
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

	private Field getField(String fieldName, Class clazz) {
		for ( Field field : clazz.getFields() ) {
			field.setAccessible( true );
			String name = field.getName();

			if ( name.equals( fieldName ) ) {
				return field;
			}
		}

		return null;
	}

	private String getDefaultName(Property property) {
		return property.getName();
	}
}
