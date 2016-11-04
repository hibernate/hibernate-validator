/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * Type argument constraint location.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
// TODO the behavior should not be based on the property name; e.g. there will not be a property name for a type
// constraint on a method parameter
class TypeArgumentConstraintLocation implements ConstraintLocation {

	private final Member member;
	private String propertyName;
	private final Type typeForValidatorResolution;

	TypeArgumentConstraintLocation( Member member, Type typeOfAnnotatedElement) {
		this.member = member;

		Class<?> type = null;
		if ( member instanceof Field ) {
			type = ( (Field) member ).getType();
		}
		else if ( member instanceof Method ) {
			type = ( (Method) member ).getReturnType();
		}

		if ( ReflectionHelper.isIterable( type ) || ReflectionHelper.isMap( type ) ) {
			this.propertyName = null;
		}
		else {
			this.propertyName = ReflectionHelper.getPropertyName( member );
		}

		this.typeForValidatorResolution = ReflectionHelper.boxedType( typeOfAnnotatedElement );
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
		if ( propertyName != null ) {
			path.addPropertyNode( propertyName );
		}
		else {
			path.addCollectionElementNode();
		}
	}

	@Override
	public String toString() {
		return "TypeArgumentValueConstraintLocation [member=" + member + ", typeForValidatorResolution="
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

		TypeArgumentConstraintLocation that = (TypeArgumentConstraintLocation) o;

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
