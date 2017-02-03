/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.internal.engine.path.PathImpl;
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

	static ConstraintLocation forField(Field field) {
		return new FieldConstraintLocation( field );
	}

	static ConstraintLocation forGetter(Method getter) {
		return new GetterConstraintLocation( getter );
	}

	static ConstraintLocation forTypeArgument(ConstraintLocation delegate, TypeVariable<?> typeParameter, Type typeOfAnnotatedElement) {
		return new TypeArgumentConstraintLocation( delegate, typeParameter, typeOfAnnotatedElement );
	}

	static ConstraintLocation forReturnValue(Executable executable) {
		return new ReturnValueConstraintLocation( executable );
	}

	static ConstraintLocation forCrossParameter(Executable executable) {
		return new CrossParameterConstraintLocation( executable );
	}

	static ConstraintLocation forParameter(Executable executable, int index) {
		return new ParameterConstraintLocation( executable, index );
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
	Member getMember();

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
	 * e.g. a bean would be passed for a {@link FieldConstraintLocation} or {@link GetterConstraintLocation} but an
	 * object array for a {@link ParameterConstraintLocation}.
	 */
	Object getValue(Object parent);
}
