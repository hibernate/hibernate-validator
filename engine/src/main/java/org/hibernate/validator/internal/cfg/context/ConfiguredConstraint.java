/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import jakarta.validation.ValidationException;

import org.hibernate.validator.cfg.AnnotationDef;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.properties.javabean.JavaBeanField;
import org.hibernate.validator.internal.properties.javabean.JavaBeanGetter;
import org.hibernate.validator.internal.util.actions.GetDeclaredMethodHandle;
import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Represents a programmatically configured constraint and meta-data
 * related to its location (bean type etc.).
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
class ConfiguredConstraint<A extends Annotation> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final MethodHandle CREATE_ANNOTATION_DESCRIPTOR_METHOD_HANDLE =
			GetDeclaredMethodHandle.andMakeAccessible( MethodHandles.lookup(), AnnotationDef.class, "createAnnotationDescriptor" );

	private final ConstraintDef<?, A> constraint;
	private final ConstraintLocation location;

	private ConfiguredConstraint(ConstraintDef<?, A> constraint, ConstraintLocation location) {
		this.constraint = constraint;
		this.location = location;
	}

	static <A extends Annotation> ConfiguredConstraint<A> forType(ConstraintDef<?, A> constraint, Class<?> beanType) {
		return new ConfiguredConstraint<>( constraint, ConstraintLocation.forClass( beanType ) );
	}

	static <A extends Annotation> ConfiguredConstraint<A> forField(ConstraintDef<?, A> constraint, JavaBeanField javaBeanField) {
		return new ConfiguredConstraint<>( constraint, ConstraintLocation.forField( javaBeanField ) );
	}

	static <A extends Annotation> ConfiguredConstraint<A> forGetter(ConstraintDef<?, A> constraint, JavaBeanGetter javaBeanGetter) {
		return forExecutable( constraint, javaBeanGetter );
	}

	public static <A extends Annotation> ConfiguredConstraint<A> forParameter(ConstraintDef<?, A> constraint, Callable callable, int parameterIndex) {
		return new ConfiguredConstraint<>( constraint, ConstraintLocation.forParameter( callable, parameterIndex ) );
	}

	public static <A extends Annotation> ConfiguredConstraint<A> forExecutable(ConstraintDef<?, A> constraint, Callable callable) {
		return new ConfiguredConstraint<>( constraint, ConstraintLocation.forReturnValue( callable ) );
	}

	public static <A extends Annotation> ConfiguredConstraint<A> forCrossParameter(ConstraintDef<?, A> constraint, Callable callable) {
		return new ConfiguredConstraint<>( constraint, ConstraintLocation.forCrossParameter( callable ) );
	}

	public static <A extends Annotation> ConfiguredConstraint<A> forTypeArgument(ConstraintDef<?, A> constraint, ConstraintLocation delegate, TypeVariable<?> typeArgument,
			Type typeOfAnnotatedElement) {
		return new ConfiguredConstraint<>(
				constraint,
				ConstraintLocation.forTypeArgument( delegate, typeArgument, typeOfAnnotatedElement )
		);
	}

	public ConstraintDef<?, A> getConstraint() {
		return constraint;
	}

	public ConstraintLocation getLocation() {
		return location;
	}

	public ConstraintAnnotationDescriptor<A> createAnnotationDescriptor() {
		try {
			@SuppressWarnings("unchecked")
			AnnotationDescriptor<A> annotationDescriptor = (AnnotationDescriptor<A>) CREATE_ANNOTATION_DESCRIPTOR_METHOD_HANDLE.invoke( constraint );
			return new ConstraintAnnotationDescriptor<>( annotationDescriptor );
		}
		catch (Throwable e) {
			if ( e instanceof ValidationException ) {
				throw (ValidationException) e;
			}
			throw LOG.getUnableToCreateAnnotationDescriptor( constraint.getClass(), e );
		}
	}

	@Override
	public String toString() {
		return constraint.toString();
	}

}
