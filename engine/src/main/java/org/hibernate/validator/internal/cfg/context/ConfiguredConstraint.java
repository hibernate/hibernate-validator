/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;

/**
 * Represents a programmatically configured constraint and meta-data
 * related to its location (bean type etc.).
 *
 * @author Gunnar Morling
 */
class ConfiguredConstraint<A extends Annotation> {

	private final ConstraintDefAccessor<A> constraint;
	private final ConstraintLocation location;
	private final ElementType elementType;

	private ConfiguredConstraint(ConstraintDef<?, A> constraint, ConstraintLocation location, ElementType elementType) {
		this.constraint = new ConstraintDefAccessor<>( constraint );
		this.location = location;
		this.elementType = elementType;
	}

	static <A extends Annotation> ConfiguredConstraint<A> forType(ConstraintDef<?, A> constraint, Class<?> beanType) {
		return new ConfiguredConstraint<>( constraint, ConstraintLocation.forClass( beanType ), ElementType.TYPE );
	}

	static <A extends Annotation> ConfiguredConstraint<A> forProperty(ConstraintDef<?, A> constraint, Member member) {
		if ( member instanceof Field ) {
			return new ConfiguredConstraint<>(
				constraint,
				ConstraintLocation.forField( (Field) member ),
				ElementType.FIELD
			);
		}
		else {
			return new ConfiguredConstraint<>(
				constraint,
				ConstraintLocation.forGetter( (Method) member ),
				ElementType.METHOD
			);
		}
	}

	public static <A extends Annotation> ConfiguredConstraint<A> forParameter(ConstraintDef<?, A> constraint, Executable executable, int parameterIndex) {
		return new ConfiguredConstraint<>(
				constraint, ConstraintLocation.forParameter( executable, parameterIndex ), ExecutableHelper.getElementType( executable )
		);
	}

	public static <A extends Annotation> ConfiguredConstraint<A> forExecutable(ConstraintDef<?, A> constraint, Executable executable) {
		return new ConfiguredConstraint<>(
				constraint, ConstraintLocation.forReturnValue( executable ), ExecutableHelper.getElementType( executable )
		);
	}

	public static <A extends Annotation> ConfiguredConstraint<A> forCrossParameter(ConstraintDef<?, A> constraint, Executable executable) {
		return new ConfiguredConstraint<>(
				constraint, ConstraintLocation.forCrossParameter( executable ), ExecutableHelper.getElementType( executable )
		);
	}

	public static <A extends Annotation> ConfiguredConstraint<A> forTypeArgument(ConstraintDef<?, A> constraint,ConstraintLocation delegate, TypeVariable<?> typeArgument, Type typeOfAnnotatedElement) {
		return new ConfiguredConstraint<>(
				constraint,
				ConstraintLocation.forTypeArgument( delegate, typeArgument, typeOfAnnotatedElement ),
				ElementType.TYPE_USE
		);
	}

	public ConstraintDef<?, A> getConstraint() {
		return constraint;
	}

	public ConstraintLocation getLocation() {
		return location;
	}

	public AnnotationDescriptor<A> createAnnotationDescriptor() {
		return constraint.createAnnotationDescriptor();
	}

	@Override
	public String toString() {
		return constraint.toString();
	}

	/**
	 * Provides access to the members of a {@link ConstraintDef}.
	 */
	private static class ConstraintDefAccessor<A extends Annotation>
			extends ConstraintDef<ConstraintDefAccessor<A>, A> {

		private ConstraintDefAccessor(ConstraintDef<?, A> original) {
			super( original );
		}

		@Override
		protected AnnotationDescriptor<A> createAnnotationDescriptor() {
			return super.createAnnotationDescriptor();
		}
	}

	public ElementType getElementType() {
		return elementType;
	}
}
