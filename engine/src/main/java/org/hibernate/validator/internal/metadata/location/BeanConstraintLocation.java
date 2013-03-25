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

import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;

/**
 * A {@link ConstraintLocation} implementation that represents either a bean (in case of class-level
 * constraints), a field or a getter method (in case of property-level constraints).
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class BeanConstraintLocation implements ConstraintLocation {

	/**
	 * The member the constraint was defined on.
	 */
	private final Member member;

	/**
	 * The class of the bean hosting this constraint.
	 */
	private final Class<?> beanClass;

	/**
	 * The type of element hosting this constraint. One of TYPE, FIELD or METHOD.
	 */
	private final ElementType elementType;

	/**
	 * The type of the annotated element
	 */
	private final Type typeOfAnnotatedElement;

	public BeanConstraintLocation(Class<?> beanClass) {
		this( beanClass, null );
	}

	public BeanConstraintLocation(Member member) {
		this( member.getDeclaringClass(), member );
	}

	/**
	 * @param beanClass The class in which the constraint is defined on
	 * @param member The member on which the constraint is defined on, {@code null} if it is a class constraint}
	 */
	private BeanConstraintLocation(Class<?> beanClass, Member member) {
		this.member = member;

		if ( this.member != null ) {
			this.elementType = ( member instanceof Method ) ? ElementType.METHOD : ElementType.FIELD;
		}
		else {
			this.elementType = ElementType.TYPE;
		}
		this.beanClass = beanClass;
		this.typeOfAnnotatedElement = determineTypeOfAnnotatedElement();
	}

	@Override
	public Class<?> getBeanClass() {
		return beanClass;
	}

	@Override
	public Member getMember() {
		return member;
	}

	@Override
	public Type typeOfAnnotatedElement() {
		return typeOfAnnotatedElement;
	}

	@Override
	public ElementType getElementType() {
		return elementType;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		BeanConstraintLocation that = (BeanConstraintLocation) o;

		if ( beanClass != null ? !beanClass.equals( that.beanClass ) : that.beanClass != null ) {
			return false;
		}
		if ( member != null ? !member.equals( that.member ) : that.member != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = member != null ? member.hashCode() : 0;
		result = 31 * result + ( beanClass != null ? beanClass.hashCode() : 0 );
		return result;
	}

	@Override
	public String toString() {
		return "BeanConstraintLocation [" + beanClass.getSimpleName() + "#" + ReflectionHelper.getPropertyName( member ) + " (" + elementType + ")]";
	}

	private Type determineTypeOfAnnotatedElement() {
		Type t;

		if ( member == null ) {
			// HV-623 - create a ParameterizedType in case the class has type parameters. Needed for constraint validator resolution (HF)
			if ( beanClass.getTypeParameters().length != 0 ) {
				t = TypeHelper.parameterizedType( beanClass, beanClass.getTypeParameters() );
			}
			else {
				t = beanClass;
			}
		}
		else {
			t = ReflectionHelper.typeOf( member );
			if ( t instanceof Class && ( (Class<?>) t ).isPrimitive() ) {
				t = ReflectionHelper.boxedType( (Class<?>) t );
			}
		}

		return t;
	}
}
