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

/**
 * Cross-parameter constraint location.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
class CrossParameterConstraintLocation implements ConstraintLocation {

	private final Callable callable;

	private final ConstraintLocationKind kind;

	CrossParameterConstraintLocation(Callable callable) {
		this.callable = callable;
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
		return Object[].class;
	}

	@Override
	public void appendTo(ExecutableParameterNameProvider parameterNameProvider, PathImpl path) {
		path.addCrossParameterNode();
	}

	@Override
	public Object getValue(Object parent) {
		return parent;
	}

	@Override
	public ConstraintLocationKind getKind() {
		return kind;
	}

	@Override
	public String toString() {
		return "CrossParameterConstraintLocation [callable=" + callable + "]";
	}

	@Override
	public int hashCode() {
		return callable.hashCode();
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
		CrossParameterConstraintLocation other = (CrossParameterConstraintLocation) obj;
		if ( !callable.equals( other.callable ) ) {
			return false;
		}

		return true;
	}
}
