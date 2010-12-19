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
package org.hibernate.validator.metadata.location;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import javax.validation.ValidationException;

import org.hibernate.validator.util.ReflectionHelper;

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
	 * The JavaBeans name of the field/property the constraint was placed on. {@code null} if this is a
	 * class level constraint.
	 */
	private final String propertyName;

	/**
	 * The class of the bean hosting this constraint.
	 */
	private final Class<?> beanClass;

	/**
	 * @param beanClass The class in which the constraint is defined on
	 * @param member The member on which the constraint is defined on, {@code null} if it is a class constraint}
	 */
	public BeanConstraintLocation(Class<?> beanClass, Member member) {

		this.member = member;

		if ( this.member != null ) {
			this.propertyName = ReflectionHelper.getPropertyName( member );
			if ( member instanceof Method && propertyName == null ) { // can happen if member is a Method which does not follow the bean convention
				throw new ValidationException(
						"Annotated methods must follow the JavaBeans naming convention. " + member.getName() + "() does not."
				);
			}
		}
		else {
			this.propertyName = null;
		}
		this.beanClass = beanClass;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public Member getMember() {
		return member;
	}

	/**
	 * @return The JavaBeans name of the field/property the constraint was placed on. {@code null} if this is a
	 *         class level constraint.
	 */
	public String getPropertyName() {
		return propertyName;
	}

	public Type typeOfAnnotatedElement() {
		Type t;

		if ( member == null ) {
			t = beanClass;
		}
		else {
			t = ReflectionHelper.typeOf( member );
			if ( t instanceof Class && ( ( Class<?> ) t ).isPrimitive() ) {
				t = ReflectionHelper.boxedType( t );
			}
		}

		return t;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "BeanConstraintLocation" );
		sb.append( "{beanClass=" ).append( beanClass );
		sb.append( ", member=" ).append( member );
		sb.append( ", propertyName='" ).append( propertyName ).append( '\'' );
		sb.append( '}' );
		return sb.toString();
	}

}
