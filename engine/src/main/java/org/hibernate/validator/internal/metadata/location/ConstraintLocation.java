/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.Member;
import java.lang.reflect.Type;

import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;

/**
 * Provides information related to the location a constraint is declared on (e.g. a bean, field or method parameter).
 * <p>
 * Note that while the validation engine works on the aggregated meta-model (which e.g. provides a unified view for
 * properties, be them represented via fields or getter methods) most of the time, in some situations the physical
 * element which hosts a constraint is relevant. This includes
 * <ul>
 * <li>retrieval of property values to be validated (either field or getter access)</li>
 * <li>constraint validator resolution; a field and the corresponding getter method may have different types, causing
 * potentially different validators to kick in for the constraints declared on either element</li>
 * <li>determination of a constraint's scope (locally defined or up in the hierarchy)</li>
 * </ul>
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class ConstraintLocation {

	/**
	 * The member the constraint was defined on.
	 */
	private final Member member;

	/**
	 * The type hosting this location.
	 */
	private final Class<?> declaringClass;

	/**
	 * The type to be used for validator resolution for constraints at this location.
	 */
	private final Type typeForValidatorResolution;

	public static ConstraintLocation forClass(Class<?> declaringClass) {
		// HV-623 - create a ParameterizedType in case the class has type parameters. Needed for constraint validator
		// resolution (HF)
		Type type = declaringClass.getTypeParameters().length == 0 ?
				declaringClass :
				TypeHelper.parameterizedType( declaringClass, declaringClass.getTypeParameters() );

		return new ConstraintLocation( declaringClass, null, type );
	}

	public static ConstraintLocation forProperty(Member member) {
		return new ConstraintLocation(
				member.getDeclaringClass(),
				member,
				ReflectionHelper.typeOf( member )
		);
	}

	public static ConstraintLocation forReturnValue(ExecutableElement executable) {
		return new ConstraintLocation(
				executable.getMember().getDeclaringClass(),
				executable.getMember(),
				ReflectionHelper.typeOf( executable.getMember() )
		);
	}

	public static ConstraintLocation forCrossParameter(ExecutableElement executable) {
		return new ConstraintLocation(
				executable.getMember().getDeclaringClass(),
				executable.getMember(),
				Object[].class
		);
	}

	public static ConstraintLocation forParameter(ExecutableElement executable, int index) {
		return new ConstraintLocation(
				executable.getMember().getDeclaringClass(),
				executable.getMember(),
				ReflectionHelper.typeOf( executable, index )
		);
	}

	private ConstraintLocation(Class<?> declaringClass, Member member, Type typeOfAnnotatedElement) {
		this.declaringClass = declaringClass;
		this.member = member;

		if ( typeOfAnnotatedElement instanceof Class && ( (Class<?>) typeOfAnnotatedElement ).isPrimitive() ) {
			this.typeForValidatorResolution = ReflectionHelper.boxedType( (Class<?>) typeOfAnnotatedElement );
		}
		else {
			this.typeForValidatorResolution = typeOfAnnotatedElement;
		}
	}

	/**
	 * Returns the class hosting this location.
	 *
	 * @return the class hosting this location
	 */
	public Class<?> getDeclaringClass() {
		return declaringClass;
	}

	/**
	 * Returns the member represented by this location.
	 *
	 * @return the member represented by this location. Will be {@code null} when this location represents a type.
	 */
	public Member getMember() {
		return member;
	}

	/**
	 * Returns the type to be used when resolving constraint validators for constraints at this location. Note that this
	 * is not always the same type as the type of the element described by this location; E.g. the wrapper type will is
	 * used for constraint validator resolution, if a constraint is declared in an element with a primitive type.
	 *
	 * @return The type to be used when resolving constraint validators for constraints at this location
	 */
	public Type getTypeForValidatorResolution() {
		return typeForValidatorResolution;
	}

	@Override
	public String toString() {
		return "ConstraintLocation [member=" + member + ", declaringClass="
				+ declaringClass + ", typeForValidatorResolution="
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

		ConstraintLocation that = (ConstraintLocation) o;

		if ( !declaringClass.equals( that.declaringClass ) ) {
			return false;
		}
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
		result = 31 * result + declaringClass.hashCode();
		result = 31 * result + typeForValidatorResolution.hashCode();
		return result;
	}
}
