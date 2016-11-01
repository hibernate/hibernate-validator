/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.Member;
import java.lang.reflect.Type;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * Property constraint location (field or getter).
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
class PropertyConstraintLocation implements ConstraintLocation {

	/**
	 * The member the constraint was defined on.
	 */
	private final Member member;

	/**
	 * The property name associated with the member.
	 */
	private final String propertyName;

	/**
	 * The type to be used for validator resolution for constraints at this location.
	 */
	private final Type typeForValidatorResolution;

	PropertyConstraintLocation(Member member) {
		this.member = member;
		this.propertyName = member == null ? null : ReflectionHelper.getPropertyName( member );
		this.typeForValidatorResolution = ReflectionHelper.boxedType( ReflectionHelper.typeOf( member ) );
	}

	@Override
	public Class<?> getDeclaringClass() {
		return member.getDeclaringClass();
	}

	@Override
	public Member getMember() {
		return member;
	}

	@Override
	public String getPropertyName() {
		return propertyName;
	}

	@Override
	public Type getTypeForValidatorResolution() {
		return typeForValidatorResolution;
	}

	@Override
	public void appendTo(ExecutableParameterNameProvider parameterNameProvider, PathImpl path) {
		path.addPropertyNode( propertyName );
	}

	@Override
	public String toString() {
		return "PropertyConstraintLocation [member=" + member + ", typeForValidatorResolution="
				+ typeForValidatorResolution + "]";
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		PropertyConstraintLocation that = (PropertyConstraintLocation) o;

		if ( member != null ? !member.equals( that.member ) : that.member != null ) {
			return false;
		}
		if ( !typeForValidatorResolution.equals( that.typeForValidatorResolution ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = member != null ? member.hashCode() : 0;
		result = 31 * result + typeForValidatorResolution.hashCode();
		return result;
	}
}
