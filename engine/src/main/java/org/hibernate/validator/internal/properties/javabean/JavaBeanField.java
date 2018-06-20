/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.javabean;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.internal.properties.PropertyAccessor;
import org.hibernate.validator.internal.properties.javabean.accessors.JavaBeanPropertyAccessorFactory;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * @author Marko Bekhta
 */
public class JavaBeanField implements org.hibernate.validator.internal.properties.Field, JavaBeanAnnotatedConstrainable {

	private final JavaBeanPropertyAccessorFactory propertyAccessorFactory;

	private final Field field;
	private final Type typeForValidatorResolution;
	private final Type type;

	public JavaBeanField(JavaBeanPropertyAccessorFactory propertyAccessorFactory, Field field) {
		this.propertyAccessorFactory = propertyAccessorFactory;
		this.field = field;
		this.type = ReflectionHelper.typeOf( field );
		this.typeForValidatorResolution = ReflectionHelper.boxedType( this.type );
	}

	@Override
	public String getName() {
		return field.getName();
	}

	@Override
	public Class<?> getDeclaringClass() {
		return field.getDeclaringClass();
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public Type getTypeForValidatorResolution() {
		return typeForValidatorResolution;
	}

	@Override
	public String getPropertyName() {
		return getName();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return field.getAnnotatedType();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return field.getDeclaredAnnotations();
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		return field.getAnnotation( annotationClass );
	}

	@Override
	public Type getGenericType() {
		return ReflectionHelper.typeOf( field );
	}

	@Override
	public TypeVariable<?>[] getTypeParameters() {
		return field.getType().getTypeParameters();
	}

	@Override
	public PropertyAccessor createAccessor() {
		return propertyAccessorFactory.forField( field );
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || this.getClass() != o.getClass() ) {
			return false;
		}

		JavaBeanField that = (JavaBeanField) o;

		if ( !this.field.equals( that.field ) ) {
			return false;
		}
		if ( !this.typeForValidatorResolution.equals( that.typeForValidatorResolution ) ) {
			return false;
		}
		return this.type.equals( that.type );
	}

	@Override
	public int hashCode() {
		int result = this.field.hashCode();
		result = 31 * result + this.typeForValidatorResolution.hashCode();
		result = 31 * result + this.type.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return getName();
	}


}
