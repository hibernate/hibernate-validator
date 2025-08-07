/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.Type;

import org.hibernate.validator.internal.engine.path.ModifiablePath;
import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;

/**
 * Executable return value constraint location.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
class ReturnValueConstraintLocation implements ConstraintLocation {

	private final Callable callable;

	private final ConstraintLocationKind kind;

	ReturnValueConstraintLocation(Callable callable) {
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
		return callable.getTypeForValidatorResolution();
	}

	@Override
	public void appendTo(ExecutableParameterNameProvider parameterNameProvider, ModifiablePath path) {
		path.addReturnValueNode();
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
		return "ReturnValueConstraintLocation [callable=" + callable + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + callable.hashCode();
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
		ReturnValueConstraintLocation other = (ReturnValueConstraintLocation) obj;
		if ( !callable.equals( other.callable ) ) {
			return false;
		}

		return true;
	}
}
