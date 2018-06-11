/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.Type;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * Parameter constraint location.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class ParameterConstraintLocation implements ConstraintLocation {

	private final Callable callable;
	private final int index;
	private final Type typeForValidatorResolution;
	private final ConstraintLocationKind kind;

	public ParameterConstraintLocation(Callable callable, int index) {
		this.callable = callable;
		this.index = index;
		this.typeForValidatorResolution = ReflectionHelper.boxedType( callable.getParameterGenericType( index ) );
		this.kind = ConstraintLocationKind.of( callable.getConstrainedElementKind() );
	}

	@Override
	public Class<?> getDeclaringClass() {
		return callable.getDeclaringClass();
	}

	@Override
	public Constrainable getConstrainable() {
		return callable;
	}

	@Override
	public Type getTypeForValidatorResolution() {
		return typeForValidatorResolution;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public void appendTo(ExecutableParameterNameProvider parameterNameProvider, PathImpl path) {
		path.addParameterNode( callable.getParameterName( parameterNameProvider, index ), index );
	}

	@Override
	public Object getValue(Object parent) {
		return ( (Object[]) parent )[index];
	}

	@Override
	public ConstraintLocationKind getKind() {
		return kind;
	}

	@Override
	public String toString() {
		return "ParameterConstraintLocation [callable=" + callable + ", index=" + index + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + callable.hashCode();
		result = prime * result + index;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		ParameterConstraintLocation other = (ParameterConstraintLocation) obj;
		if ( !callable.equals( other.callable ) ) {
			return false;
		}
		if ( index != other.index ) {
			return false;
		}
		return true;
	}
}
