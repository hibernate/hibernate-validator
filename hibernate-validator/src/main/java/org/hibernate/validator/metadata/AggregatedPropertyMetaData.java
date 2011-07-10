/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
import java.util.Iterator;
import java.util.Set;

import org.hibernate.validator.util.ReflectionHelper;

import static org.hibernate.validator.util.CollectionHelper.newHashSet;

/**
 * @author Gunnar Morling
 */
public class AggregatedPropertyMetaData implements Iterable<BeanMetaConstraint<?>> {

	private final PropertyMetaData root;

	private final Set<BeanMetaConstraint<?>> constraints;

	private final Set<Member> cascadingMembers;

	private AggregatedPropertyMetaData(PropertyMetaData root, Set<BeanMetaConstraint<?>> constraints, Set<Member> cascadingMembers) {
		this.root = root;
		this.constraints = constraints;
		this.cascadingMembers = cascadingMembers;
	}

	public PropertyMetaData getRoot() {
		return root;
	}

	public boolean isCascading() {
		return !cascadingMembers.isEmpty();
	}

	public Set<Member> getCascadingMembers() {
		return cascadingMembers;
	}

	public Iterator<BeanMetaConstraint<?>> iterator() {
		return constraints.iterator();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ( ( cascadingMembers == null ) ? 0 : cascadingMembers.hashCode() );
		result = prime * result
				+ ( ( constraints == null ) ? 0 : constraints.hashCode() );
		result = prime * result + ( ( root == null ) ? 0 : root.hashCode() );
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
		AggregatedPropertyMetaData other = (AggregatedPropertyMetaData) obj;
		if ( cascadingMembers == null ) {
			if ( other.cascadingMembers != null ) {
				return false;
			}
		}
		else if ( !cascadingMembers.equals( other.cascadingMembers ) ) {
			return false;
		}
		if ( constraints == null ) {
			if ( other.constraints != null ) {
				return false;
			}
		}
		else if ( !constraints.equals( other.constraints ) ) {
			return false;
		}
		if ( root == null ) {
			if ( other.root != null ) {
				return false;
			}
		}
		else if ( !root.equals( other.root ) ) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AggregatedPropertyMetaData [root=" + root + ", constraints="
				+ constraints + ", cascadingMembers=" + cascadingMembers + "]";
	}

	public static class Builder {

		private final ConstraintHelper constraintHelper;

		private final PropertyMetaData root;

		private final Set<BeanMetaConstraint<?>> constraints;

		private Set<Member> cascadingMembers;

		public Builder(ConstraintHelper constraintHelper, PropertyMetaData propertyMetaData) {
			this.constraintHelper = constraintHelper;
			this.root = propertyMetaData;
			constraints = newHashSet();
			constraints.addAll( propertyMetaData.getConstraints() );
			cascadingMembers = newHashSet();
			if ( propertyMetaData.isCascading() ) {
				cascadingMembers.add( propertyMetaData.getLocation().getMember() );
			}
		}

		public boolean accepts(PropertyMetaData propertyMetaData) {

			String propertyName1 = ReflectionHelper.getPropertyName( propertyMetaData.getLocation().getMember() );
			String propertyName2 = ReflectionHelper.getPropertyName( root.getLocation().getMember() );

			return
					propertyMetaData.getLocation().getBeanClass().isAssignableFrom( root.getLocation().getBeanClass() )
							&& ( ( propertyName1 != null && propertyName1.equals( propertyName2 ) ) ||
							propertyName1 == null && propertyName2 == null );
		}

		public void add(PropertyMetaData propertyMetaData) {
			constraints.addAll( propertyMetaData.getConstraints() );
			if ( propertyMetaData.isCascading() ) {
				cascadingMembers.add( propertyMetaData.getLocation().getMember() );
			}
		}

		public AggregatedPropertyMetaData build() {

			Set<BeanMetaConstraint<?>> adaptedConstraints = newHashSet();

			for ( BeanMetaConstraint<?> oneConstraint : constraints ) {
				adaptedConstraints.add(
						adaptOriginAndImplicitGroup(
								root.getLocation().getBeanClass(), oneConstraint
						)
				);
			}

			return new AggregatedPropertyMetaData( root, adaptedConstraints, cascadingMembers );
		}

		/**
		 * Adapts the given constraint to the given bean type. In case the
		 * constraint is defined locally at the bean class the original constraint
		 * will be returned without any modifications. If the constraint is defined
		 * in the hierarchy (interface or super class) a new constraint will be
		 * returned with an origin of {@link ConstraintOrigin#DEFINED_IN_HIERARCHY}.
		 * If the constraint is defined on an interface, the interface type will
		 * additionally be part of the constraint's groups (implicit grouping).
		 *
		 * @param <A> The type of the constraint's annotation.
		 * @param beanClass The bean type to which the constraint shall be adapted.
		 * @param constraint The constraint that shall be adapted. This constraint itself
		 * will not be altered.
		 *
		 * @return A constraint adapted to the given bean type.
		 */
		private <A extends Annotation> BeanMetaConstraint<A> adaptOriginAndImplicitGroup(Class<?> beanClass, BeanMetaConstraint<A> constraint) {

			ConstraintOrigin definedIn = definedIn( beanClass, constraint.getLocation().getBeanClass() );

			if ( definedIn == ConstraintOrigin.DEFINED_LOCALLY ) {
				return constraint;
			}

			Class<?> constraintClass = constraint.getLocation().getBeanClass();

			ConstraintDescriptorImpl<A> descriptor = new ConstraintDescriptorImpl<A>(
					(A) constraint.getDescriptor().getAnnotation(),
					constraintHelper,
					constraintClass.isInterface() ? constraintClass : null,
					constraint.getElementType(),
					definedIn
			);

			return new BeanMetaConstraint<A>(
					descriptor,
					constraint.getLocation()
			);
		}

		/**
		 * @param rootClass The root class. That is the class for which we currently create a  {@code BeanMetaData}
		 * @param hierarchyClass The class on which the current constraint is defined on
		 *
		 * @return Returns {@code ConstraintOrigin.DEFINED_LOCALLY} if the constraint was defined on the root bean,
		 *         {@code ConstraintOrigin.DEFINED_IN_HIERARCHY} otherwise.
		 */
		private ConstraintOrigin definedIn(Class<?> rootClass, Class<?> hierarchyClass) {
			if ( hierarchyClass.equals( rootClass ) ) {
				return ConstraintOrigin.DEFINED_LOCALLY;
			}
			else {
				return ConstraintOrigin.DEFINED_IN_HIERARCHY;
			}
		}
	}

}
