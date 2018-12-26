/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.spi.nodenameprovider;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
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
	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Class<? extends Annotation> annotationType;
	private final String annotationMemberName;

	AnnotationPropertyNodeNameProvider(Class<? extends Annotation> annotationType) {
		this( annotationType, VALUE );
	}

	AnnotationPropertyNodeNameProvider(Class<? extends Annotation> annotationType, String annotationMemberName) {
		this.annotationType = Objects.requireNonNull( annotationType );
		this.annotationMemberName = Objects.requireNonNull( annotationMemberName );
	}

	@Override
	public String getName(Property property) {
		if ( property instanceof JavaBeanProperty ) {
			return getJavaBeanPropertyName( (JavaBeanProperty) property );
		}

		return getDefaultName( property );
	}

	private String getJavaBeanPropertyName(JavaBeanProperty property) {
		Optional<Field> field = getField( property );

		if ( field.isPresent() && field.get().isAnnotationPresent( annotationType ) ) {
			return getAnnotationMemberValue( field.get(), annotationMemberName )
					.orElse( getDefaultName( property ) );
		}
		else {
			return getDefaultName( property );
		}
	}

	private Optional<Field> getField(JavaBeanProperty property) {
		return Arrays.stream( property.getDeclaringClass().getFields() )
				.peek( field -> field.setAccessible( true ) )
				.filter( field -> property.getName().equals( field.getName() ) )
				.findFirst();
	}

	private Optional<String> getAnnotationMemberValue(Field field, String annotationMemberName) {
		Annotation annotation = field.getAnnotation( annotationType );

		try {
			return Optional.of(
					(String) annotation.annotationType().getMethod( annotationMemberName ).invoke( annotation ) );
		}
		catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			LOG.error( "Unable to get annotation member value", e );

			return Optional.empty();
		}
	}

	private String getDefaultName(Property property) {
		return property.getName();
	}
}
