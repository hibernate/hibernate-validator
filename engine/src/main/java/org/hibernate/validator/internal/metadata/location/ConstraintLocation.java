/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.annotation.ElementType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.properties.javabean.JavaBeanField;
import org.hibernate.validator.internal.properties.javabean.JavaBeanGetter;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;

/**
 * Represents the location (e.g. a bean, field or method parameter) of a constraint and provides logic related to it,
 * e.g. for appending the location to a given property path.
 * <p>
 * Note that while the validation engine works on the aggregated meta-model (which e.g. provides a unified view for
 * properties, be them represented via fields or getter methods) most of the time, in some situations the physical
 * element which hosts a constraint is relevant. This includes
 * <ul>
 * <li>retrieval of property values to be validated (either field or getter access)</li>
 * <li>constraint validator resolution; a field and the corresponding getter method may have different types, causing
 * potentially different validators to kick in for the constraints declared on either element</li>
 * <li>determination of a constraint's scope (locally defined or up in the hierarchy)</li>
 * </ul>
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public interface ConstraintLocation {

	static ConstraintLocation forClass(Class<?> declaringClass) {
		return new BeanConstraintLocation( declaringClass );
	}

	static ConstraintLocation forField(JavaBeanField field) {
		return new FieldPropertyConstraintLocation( field );
	}

	static ConstraintLocation forGetter(JavaBeanGetter getter) {
		return new GetterPropertyConstraintLocation( getter );
	}

	static ConstraintLocation forTypeArgument(ConstraintLocation delegate, TypeVariable<?> typeParameter, Type typeOfAnnotatedElement) {
		return new TypeArgumentConstraintLocation( delegate, typeParameter, typeOfAnnotatedElement );
	}

	static ConstraintLocation forReturnValue(Callable callable) {
		return new ReturnValueConstraintLocation( callable );
	}

	static ConstraintLocation forCrossParameter(Callable callable) {
		return new CrossParameterConstraintLocation( callable );
	}

	static ConstraintLocation forParameter(Callable callable, int index) {
		return new ParameterConstraintLocation( callable, index );
	}

	/**
	 * Returns the class hosting this location.
	 */
	Class<?> getDeclaringClass();

	/**
	 * Returns the member represented by this location.
	 *
	 * @return the member represented by this location. Will be {@code null} when this location represents a type.
	 */
	Constrainable getConstrainable();

	/**
	 * Returns the type to be used when resolving constraint validators for constraints at this location. Note that this
	 * is not always the same type as the type of the element described by this location; E.g. the wrapper type will is
	 * used for constraint validator resolution, if a constraint is declared in an element with a primitive type.
	 *
	 * @return The type to be used when resolving constraint validators for constraints at this location
	 */
	Type getTypeForValidatorResolution();

	/**
	 * Appends a node representing this location to the given property path.
	 */
	void appendTo(ExecutableParameterNameProvider parameterNameProvider, PathImpl path);

	/**
	 * Obtains the value of this location from the parent. The type of the passed parent depends on the location type,
	 * e.g. a bean would be passed for a {@link PropertyConstraintLocation} but an
	 * object array for a {@link ParameterConstraintLocation}.
	 */
	Object getValue(Object parent);

	/**
	 * Returns the nature of the constraint location.
	 */
	ConstraintLocationKind getKind();

	enum ConstraintLocationKind {
		TYPE( ElementType.TYPE ),
		CONSTRUCTOR( ElementType.CONSTRUCTOR ),
		METHOD( ElementType.METHOD ),
		PARAMETER( ElementType.PARAMETER ),
		FIELD( ElementType.FIELD ),
		TYPE_USE( ElementType.TYPE_USE ),
		;

		private final ElementType elementType;

		ConstraintLocationKind(ElementType elementType) {
			this.elementType = elementType;
		}

		public ElementType getElementType() {
			return elementType;
		}
	}
}
