/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.properties.javabean;

import static org.hibernate.validator.internal.util.TypeHelper.isHibernateValidatorEnhancedBean;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.engine.HibernateValidatorEnhancedBean;
import org.hibernate.validator.internal.properties.PropertyAccessor;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.actions.GetDeclaredField;

/**
 * @author Marko Bekhta
 */
public class JavaBeanField implements org.hibernate.validator.internal.properties.Field, JavaBeanAnnotatedConstrainable {

	private final Field field;
	private final String resolvedPropertyName;
	private final Type typeForValidatorResolution;
	private final Type type;

	public JavaBeanField(Field field, String resolvedPropertyName) {
		this.field = field;
		this.type = ReflectionHelper.typeOf( field );
		this.typeForValidatorResolution = ReflectionHelper.boxedType( this.type );
		this.resolvedPropertyName = resolvedPropertyName;
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
	public String getResolvedPropertyName() {
		return resolvedPropertyName;
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
		if ( isHibernateValidatorEnhancedBean( field.getDeclaringClass() ) ) {
			return new EnhancedBeanFieldAccessor( field.getName() );
		}
		else {
			return new FieldAccessor( field );
		}
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

	private static class EnhancedBeanFieldAccessor implements PropertyAccessor {

		private final String name;

		private EnhancedBeanFieldAccessor(final String name) {
			this.name = name;
		}

		@Override
		public Object getValueFrom(Object bean) {
			// we don't do an instanceof check here as it should already be applied when the accessor was created.
			return ( (HibernateValidatorEnhancedBean) bean ).$$_hibernateValidator_getFieldValue( name );
		}
	}

	private static class FieldAccessor implements PropertyAccessor {

		private Field accessibleField;

		private FieldAccessor(Field field) {
			this.accessibleField = getAccessible( field );
		}

		@Override
		public Object getValueFrom(Object bean) {
			return ReflectionHelper.getValue( accessibleField, bean );
		}
	}

	/**
	 * Returns an accessible copy of the given member.
	 */
	private static Field getAccessible(Field original) {
		Class<?> clazz = original.getDeclaringClass();

		return GetDeclaredField.andMakeAccessible( clazz, original.getName() );
	}
}
