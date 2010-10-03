// $Id$// $Id$
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
package org.hibernate.validator.metadata;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.validator.engine.ConstraintTree;
import org.hibernate.validator.engine.ValidationContext;
import org.hibernate.validator.engine.ValueContext;
import org.hibernate.validator.metadata.site.ConstraintSite;

/**
 * Instances of this class abstract the constraint type  (class, method or field constraint) and give access to
 * meta data about the constraint. This allows a unified handling of constraints in the validator implementation.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
//TODO GM: parametrize with type of constraint site
public class MetaConstraint<T, A extends Annotation> {

	/**
	 * The constraint tree created from the constraint annotation.
	 */
	private final ConstraintTree<A> constraintTree;
	
	/**
	 * The site at which this constraint is defined.
	 */
	private final ConstraintSite site;
	
	/**
	 * @param beanClass The class in which the constraint is defined on
	 * @param member The member on which the constraint is defined on, {@code null} if it is a class constraint}
	 * @param constraintDescriptor The constraint descriptor for this constraint
	 */
	public MetaConstraint(ConstraintDescriptorImpl<A> constraintDescriptor, ConstraintSite site) {
		
		constraintTree = new ConstraintTree<A>( constraintDescriptor );
		this.site = site;
	}

	/**
	 * @return Returns the list of groups this constraint is part of. This might include the default group even when
	 *         it is not explicitly specified, but part of the redefined default group list of the hosting bean.
	 */
	public Set<Class<?>> getGroupList() {
		return constraintTree.getDescriptor().getGroups();
	}

	public ConstraintDescriptorImpl<A> getDescriptor() {
		return constraintTree.getDescriptor();
	}

	public ElementType getElementType() {
		return constraintTree.getDescriptor().getElementType();
	}

	public <T, U, V> boolean validateConstraint(ValidationContext<T> executionContext, ValueContext<U, V> valueContext) {
		valueContext.setElementType( getElementType() );
		constraintTree.validateConstraints(
				typeOfAnnotatedElement(), executionContext, valueContext
		);

		return !executionContext.hasFailures();
	}

	public ConstraintSite getSite() {
		return site;
	}
	
	/**
	 * @param o the object from which to retrieve the value.
	 *
	 * @return Returns the value for this constraint from the specified object. Depending on the type either the value itself
	 *         is returned of method or field access is used to access the value.
	 */
	public Object getValue(Object o) {
		return site.getValue(o);
	}

	protected Type typeOfAnnotatedElement() {
		return site.typeOfAnnotatedElement();
	}

	@Override
	public String toString() {
		return "MetaConstraint [constraintTree=" + constraintTree + ", site="
				+ site + "]";
	}

}
