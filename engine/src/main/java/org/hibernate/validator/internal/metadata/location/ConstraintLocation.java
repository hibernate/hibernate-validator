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
 * Describes the location at which a constraint is specified (a bean, a method parameter etc.).
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
	 * The class of the bean hosting this constraint.
	 */
	private final Class<?> beanClass;

	/**
	 * The type of the annotated element
	 */
	private final Type typeOfAnnotatedElement;

	public static ConstraintLocation forClass(Class<?> beanClass) {
		// HV-623 - create a ParameterizedType in case the class has type parameters. Needed for constraint validator
		// resolution (HF)
		Type type = beanClass.getTypeParameters().length == 0 ?
				beanClass :
				TypeHelper.parameterizedType( beanClass, beanClass.getTypeParameters() );

		return new ConstraintLocation( beanClass, null, type );
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

	private ConstraintLocation(Class<?> beanClass, Member member, Type typeOfAnnotatedElement) {
		this.beanClass = beanClass;
		this.member = member;

		if ( typeOfAnnotatedElement instanceof Class && ( (Class<?>) typeOfAnnotatedElement ).isPrimitive() ) {
			this.typeOfAnnotatedElement = ReflectionHelper.boxedType( (Class<?>) typeOfAnnotatedElement );
		}
		else {
			this.typeOfAnnotatedElement = typeOfAnnotatedElement;
		}
	}

	/**
	 * Returns the class on which the constraint is defined.
	 *
	 * @return the class on which the constraint is defined.
	 */
	public Class<?> getBeanClass() {
		return beanClass;
	}

	/**
	 * Returns the member the constraint is defined on.
	 *
	 * @return the member the constraint is defined on.
	 */
	public Member getMember() {
		return member;
	}

	/**
	 * Returns the type of the element at this constraint location. Depending
	 * on the concrete implementation this might be the type of an annotated bean, method parameter etc.
	 *
	 * @return The type of the element at this constraint location.
	 */
	public Type typeOfAnnotatedElement() {
		return typeOfAnnotatedElement;
	}

	@Override
	public String toString() {
		return "ConstraintLocation [member=" + member + ", beanClass="
				+ beanClass + ", typeOfAnnotatedElement="
				+ typeOfAnnotatedElement + "]";
	}
}
