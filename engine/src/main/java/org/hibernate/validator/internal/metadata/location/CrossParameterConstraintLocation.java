/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;

/**
 * Cross-parameter constraint location.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
class CrossParameterConstraintLocation implements ConstraintLocation {

	private final Executable executable;

	CrossParameterConstraintLocation(Executable executable) {
		this.executable = executable;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return executable.getDeclaringClass();
	}

	@Override
	public Member getMember() {
		return executable;
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
	public String toString() {
		return "CrossParameterConstraintLocation [executable=" + executable + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( executable == null ) ? 0 : executable.hashCode() );
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
		CrossParameterConstraintLocation other = (CrossParameterConstraintLocation) obj;
		if ( executable == null ) {
			if ( other.executable != null ) {
				return false;
			}
		}
		else if ( !executable.equals( other.executable ) ) {
			return false;
		}

		return true;
	}
}
