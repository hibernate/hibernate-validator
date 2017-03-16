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
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * A type parameter that is marked for cascaded validation and/or has one or more nested type parameters marked for
 * cascaded validation.
 *
 * @author Guillaume Smet
 */
public class CascadingTypeParameter {

	/**
	 * The enclosing type that defines this type parameter.
	 */
	private final Type enclosingType;

	/**
	 * The type parameter.
	 */
	private final TypeVariable<?> typeParameter;

	/**
	 * Possibly the cascading type parameters corresponding to this type parameter if it is a parameterized type.
	 */
	@Immutable
	private final List<CascadingTypeParameter> nestedCascadingTypeParameters;

	/**
	 * If this type parameter is marked for cascading.
	 */
	private final boolean cascading;

	public CascadingTypeParameter(Type enclosingType, TypeVariable<?> typeParameter, boolean cascading,
			List<CascadingTypeParameter> nestedCascadingTypeParameters) {
		this.enclosingType = enclosingType;
		this.typeParameter = typeParameter;
		this.cascading = cascading;
		this.nestedCascadingTypeParameters = CollectionHelper.toImmutableList( nestedCascadingTypeParameters );
	}

	public static CascadingTypeParameter annotatedObject(Type cascadableType) {
		return new CascadingTypeParameter( cascadableType, AnnotatedObject.INSTANCE, true, Collections.emptyList() );
	}

	public static CascadingTypeParameter arrayElement(Type cascadableType) {
		return new CascadingTypeParameter( cascadableType, new ArrayElement( cascadableType ), true,
				Collections.emptyList() );
	}

	public TypeVariable<?> getTypeParameter() {
		return typeParameter;
	}

	public Type getEnclosingType() {
		return enclosingType;
	}

	public boolean isCascading() {
		return cascading;
	}

	public List<CascadingTypeParameter> getNestedCascadingTypeParameters() {
		return nestedCascadingTypeParameters;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( getClass().getSimpleName() );
		sb.append( " [" );
		sb.append( "enclosingType=" ).append( StringHelper.toShortString( enclosingType ) ).append( ", " );
		sb.append( "typeParameter=" ).append( typeParameter ).append( ", " );
		sb.append( "cascading=" ).append( cascading ).append( ", " );
		sb.append( "nestedCascadingTypeParameters=" ).append( nestedCascadingTypeParameters );
		sb.append( "]" );
		return sb.toString();
	}

}
