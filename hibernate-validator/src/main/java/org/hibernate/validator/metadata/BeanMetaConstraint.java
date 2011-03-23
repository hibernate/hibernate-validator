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
package org.hibernate.validator.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;

import org.hibernate.validator.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.util.ReflectionHelper;

/**
 * A {@link MetaConstraint} implementation that represents a field,
 * property-level or class level constraint and allows the unified handling of
 * these constraints.
 *
 * @author Gunnar Morling
 */
public class BeanMetaConstraint<A extends Annotation> extends MetaConstraint<A> {

	/**
	 * Creates a new {@link BeanMetaConstraint}.
	 *
	 * @param constraintDescriptor The descriptor for this constraint.
	 * @param beanClass The type hosting this constraint.
	 * @param member The field or getter method hosting this constraint if this is
	 * not a class-level constraint, otherwise null.
	 */
	public BeanMetaConstraint(ConstraintDescriptorImpl<A> constraintDescriptor, Class<?> beanClass, Member member) {
		super( constraintDescriptor, new BeanConstraintLocation( beanClass, member ) );
	}

	/**
	 * Returns the location of this constraint.
	 */
	public BeanConstraintLocation getLocation() {
		return (BeanConstraintLocation) location;
	}

	/**
	 * @param o the object from which to retrieve the value.
	 *
	 * @return Returns the value for this constraint from the specified object. Depending on the type either the value itself
	 *         is returned of method or field access is used to access the value.
	 */
	public Object getValue(Object o) {

		if ( getLocation().getMember() == null ) {
			return o;
		}
		else {
			return ReflectionHelper.getValue( getLocation().getMember(), o );
		}
	}
}
