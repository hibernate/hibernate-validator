/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.StringHelper;

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
	private final Type containerType;
	private final ConstraintLocation outerDelegate;

	TypeArgumentConstraintLocation(ConstraintLocation delegate, TypeVariable<?> typeParameter, Type typeOfAnnotatedElement) {
		this.delegate = delegate;
		this.typeParameter = typeParameter;
		this.typeForValidatorResolution = ReflectionHelper.boxedType( typeOfAnnotatedElement );
		this.containerType = delegate.getTypeForValidatorResolution();

		ConstraintLocation outerDelegate = delegate;
		while ( outerDelegate instanceof TypeArgumentConstraintLocation ) {
			outerDelegate = ( (TypeArgumentConstraintLocation) outerDelegate ).delegate;
		}
		this.outerDelegate = outerDelegate;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return delegate.getDeclaringClass();
	}

	@Override
	public Member getMember() {
		return delegate.getMember();
	}

	public TypeVariable<?> getTypeParameter() {
		return typeParameter;
	}

	@Override
	public Type getTypeForValidatorResolution() {
		return typeForValidatorResolution;
	}

	public Type getContainerType() {
		return containerType;
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

		if ( delegate != null ? !delegate.equals( that.delegate ) : that.delegate != null ) {
			return false;
		}
		if ( !typeForValidatorResolution.equals( that.typeForValidatorResolution ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = delegate != null ? delegate.hashCode() : 0;
		result = 31 * result + typeForValidatorResolution.hashCode();
		return result;
	}
}
