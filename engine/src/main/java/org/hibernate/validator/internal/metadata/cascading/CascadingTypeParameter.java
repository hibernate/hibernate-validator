/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.cascading;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.List;

import org.hibernate.validator.internal.engine.cascading.AnnotatedObject;
import org.hibernate.validator.internal.engine.cascading.ArrayElement;

/**
 * A type parameter that is marked for cascaded validation.
 *
 * @author Guillaume Smet
 */
public class CascadingTypeParameter {

	/**
	 * The type parameter.
	 */
	private final TypeVariable<?> typeParameter;

	/**
	 * The enclosing type that defines this type parameter.
	 */
	private final Type cascadableType;

	/**
	 * Possibly the cascading type parameters corresponding to this type parameter if it is a parameterized type.
	 */
	private final List<CascadingTypeParameter> cascadingTypeParameters;

	public CascadingTypeParameter(TypeVariable<?> typeParameter, Type cascadableType, List<CascadingTypeParameter> cascadingTypeParameters) {
		this.typeParameter = typeParameter;
		this.cascadableType = cascadableType;
		this.cascadingTypeParameters = cascadingTypeParameters;
	}

	public static CascadingTypeParameter annotatedObject(Type cascadableType) {
		return new CascadingTypeParameter( AnnotatedObject.INSTANCE, cascadableType, Collections.emptyList() );
	}

	public static CascadingTypeParameter arrayElement(Type cascadableType) {
		return new CascadingTypeParameter( ArrayElement.INSTANCE, cascadableType, Collections.emptyList() );
	}

	public TypeVariable<?> getTypeParameter() {
		return typeParameter;
	}

	public Type getEnclosingType() {
		return cascadableType;
	}

	public List<CascadingTypeParameter> getCascadingTypeParameters() {
		return cascadingTypeParameters;
	}

}
