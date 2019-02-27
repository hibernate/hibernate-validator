/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.TypeHelper;

/**
 * Type argument constraint location.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class TypeArgumentConstraintLocation implements ConstraintLocation {

	private final ConstraintLocation delegate;
	private final TypeVariable<?> typeParameter;
	private final Type typeForValidatorResolution;
	private final Class<?> containerClass;
	private final ConstraintLocation outerDelegate;
	private final int hashCode;

	TypeArgumentConstraintLocation(ConstraintLocation delegate, TypeVariable<?> typeParameter, Type typeOfAnnotatedElement) {
		this.delegate = delegate;
		this.typeParameter = typeParameter;
		this.typeForValidatorResolution = ReflectionHelper.boxedType( typeOfAnnotatedElement );
		this.containerClass = TypeHelper.getErasedReferenceType( delegate.getTypeForValidatorResolution() );

		ConstraintLocation outerDelegate = delegate;
		while ( outerDelegate instanceof TypeArgumentConstraintLocation ) {
			outerDelegate = ( (TypeArgumentConstraintLocation) outerDelegate ).delegate;
		}
		this.outerDelegate = outerDelegate;
		this.hashCode = buildHashCode( delegate, typeParameter );
	}

	@Override
	public Class<?> getDeclaringClass() {
		return delegate.getDeclaringClass();
	}

	@Override
	public Constrainable getConstrainable() {
		return delegate.getConstrainable();
	}

	public TypeVariable<?> getTypeParameter() {
		return typeParameter;
	}

	@Override
	public Type getTypeForValidatorResolution() {
		return typeForValidatorResolution;
	}

	public Class<?> getContainerClass() {
		return containerClass;
	}

	@Override
	public void appendTo(ExecutableParameterNameProvider parameterNameProvider, PathImpl path) {
		delegate.appendTo( parameterNameProvider, path );
	}

	@Override
	public Object getValue(Object parent) {
		return delegate.getValue( parent );
	}

	public ConstraintLocation getDelegate() {
		return delegate;
	}

	public ConstraintLocation getOuterDelegate() {
		return outerDelegate;
	}

	@Override
	public ConstraintLocationKind getKind() {
		return ConstraintLocationKind.TYPE_USE;
	}

	@Override
	public String toString() {
		return "TypeArgumentValueConstraintLocation [typeForValidatorResolution=" + StringHelper.toShortString( typeForValidatorResolution )
				+ ", delegate=" + delegate + "]";
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		TypeArgumentConstraintLocation that = (TypeArgumentConstraintLocation) o;

		if ( !typeParameter.equals( that.typeParameter ) ) {
			return false;
		}
		if ( !delegate.equals( that.delegate ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	private static int buildHashCode(ConstraintLocation delegate, TypeVariable<?> typeParameter) {
		int result = delegate.hashCode();
		result = 31 * result + typeParameter.hashCode();
		return result;
	}
}
