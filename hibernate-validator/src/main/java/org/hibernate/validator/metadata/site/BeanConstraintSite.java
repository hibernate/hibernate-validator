// $Id: MetaConstraint.java 19313 2010-04-28 11:05:26Z hardy.ferentschik $// $Id: MetaConstraint.java 19313 2010-04-28 11:05:26Z hardy.ferentschik $
/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.metadata.site;

import java.lang.reflect.Member;
import java.lang.reflect.Type;

import org.hibernate.validator.util.ReflectionHelper;

/**
 * Instances of this class abstract the constraint type  (class, method or field constraint) and give access to
 * meta data about the constraint. This allows a unified handling of constraints in the validator implementation.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class BeanConstraintSite<T> implements ConstraintSite {

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
	private final Class<T> beanClass;

	/**
	 * @param beanClass The class in which the constraint is defined on
	 * @param member The member on which the constraint is defined on, {@code null} if it is a class constraint}
	 */
	public BeanConstraintSite(Class<T> beanClass, Member member) {
		this.member = member;
		if ( this.member != null ) {
			this.propertyName = ReflectionHelper.getPropertyName( member );
		}
		else {
			this.propertyName = null;
		}
		this.beanClass = beanClass;
	}

	public Class<T> getBeanClass() {
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

	/**
	 * @param o the object from which to retrieve the value.
	 *
	 * @return Returns the value for this constraint from the specified object. Depending on the type either the value itself
	 *         is returned of method or field access is used to access the value.
	 */
	public Object getValue(Object o) {

		if ( member == null ) {
			return o;
		}
		else {
			return ReflectionHelper.getValue( member, o );
		}
	}

	public Type typeOfAnnotatedElement() {
		Type t;

		if ( member == null ) {
			t = beanClass;
		}
		else {
			t = ReflectionHelper.typeOf( member );
			if ( t instanceof Class && ( (Class<?>) t ).isPrimitive() ) {
				t = ReflectionHelper.boxedType( t );
			}
		}

		return t;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "BeanConstraintSite" );
		sb.append( "{beanClass=" ).append( beanClass );
		sb.append( ", member=" ).append( member );
		sb.append( ", propertyName='" ).append( propertyName ).append( '\'' );
		sb.append( '}' );
		return sb.toString();
	}
}
