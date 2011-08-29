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
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.metadata.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.util.ReflectionHelper;

import static org.hibernate.validator.util.CollectionHelper.asSet;
import static org.hibernate.validator.util.CollectionHelper.newHashSet;

/**
 * @author Gunnar Morling
 */
public class PropertyMetaData extends AbstractConstraintMetaData {

	private final Class<?> type;

	private final String propertyName;

	private final Set<Member> cascadingMembers;

	private final boolean isConstrained;

	private PropertyMetaData(Class<?> type, String propertyName, Set<MetaConstraint<?>> constraints, Set<Member> cascadingMembers) {
		super( constraints, ConstraintMetaDataKind.PROPERTY );
		this.type = type;
		this.propertyName = propertyName;
		this.cascadingMembers = cascadingMembers;
		this.isConstrained = !cascadingMembers.isEmpty() || !constraints.isEmpty();
	}

	public String getPropertyName() {
		return propertyName;
	}

	public boolean isCascading() {
		return !cascadingMembers.isEmpty();
	}

	public Set<Member> getCascadingMembers() {
		return cascadingMembers;
	}

	public boolean isConstrained() {
		return isConstrained;
	}

	public PropertyDescriptor getPropertyDescriptor() {

		if ( !isConstrained ) {
			return null;
		}

		PropertyDescriptorImpl propertyDescriptor = new PropertyDescriptorImpl(
				type, isCascading(), getPropertyName(), null
		);
		for ( MetaConstraint<?> oneConstraint : constraints ) {
			propertyDescriptor.addConstraintDescriptor( oneConstraint.getDescriptor() );
		}
		return propertyDescriptor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ( ( cascadingMembers == null ) ? 0 : cascadingMembers.hashCode() );
		result = prime * result + ( isConstrained ? 1231 : 1237 );
		result = prime * result
				+ ( ( propertyName == null ) ? 0 : propertyName.hashCode() );
		result = prime * result + ( ( type == null ) ? 0 : type.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !super.equals( obj ) ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		PropertyMetaData other = (PropertyMetaData) obj;
		if ( cascadingMembers == null ) {
			if ( other.cascadingMembers != null ) {
				return false;
			}
		}
		else if ( !cascadingMembers.equals( other.cascadingMembers ) ) {
			return false;
		}
		if ( isConstrained != other.isConstrained ) {
			return false;
		}
		if ( propertyName == null ) {
			if ( other.propertyName != null ) {
				return false;
			}
		}
		else if ( !propertyName.equals( other.propertyName ) ) {
			return false;
		}
		if ( type == null ) {
			if ( other.type != null ) {
				return false;
			}
		}
		else if ( !type.equals( other.type ) ) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {

		StringBuilder cascadingMembers = new StringBuilder();

		for ( Member oneCascadingMember : this.cascadingMembers ) {
			cascadingMembers.append( oneCascadingMember.getName() );
			cascadingMembers.append( ", " );
		}

		if ( cascadingMembers.length() > 0 ) {
			cascadingMembers.subSequence( 0, cascadingMembers.length() - 2 );
		}

		return "PropertyMetaData [type=" + type.getSimpleName() + ", propertyName="
				+ propertyName + ", cascadingMembers=[" + cascadingMembers
				+ "], isConstrained=" + isConstrained + "]";
	}

	public static class Builder extends BeanMetaDataManager.Builder {

		private final ConstraintHelper constraintHelper;

		private final ConstrainedElement root;

		private final Set<MetaConstraint<?>> constraints;

		private final Set<Member> cascadingMembers;

		public Builder(ConstrainedField constrainedField, ConstraintHelper constraintHelper) {
			this.constraintHelper = constraintHelper;
			this.root = constrainedField;
			this.constraints = newHashSet( constrainedField );
			this.cascadingMembers = constrainedField.isCascading() ? asSet(
					constrainedField.getLocation()
							.getMember()
			) : new HashSet<Member>();
		}

		public Builder(ConstrainedType constrainedType, ConstraintHelper constraintHelper) {
			this.constraintHelper = constraintHelper;
			this.root = constrainedType;
			this.constraints = newHashSet( constrainedType );
			this.cascadingMembers = Collections.<Member>emptySet();
		}

		public Builder(ConstrainedMethod constrainedMethod, ConstraintHelper constraintHelper) {
			this.constraintHelper = constraintHelper;
			this.root = constrainedMethod;
			this.constraints = newHashSet( constrainedMethod );
			this.cascadingMembers = constrainedMethod.isCascading() ? asSet(
					(Member) constrainedMethod.getLocation()
							.getMethod()
			) : new HashSet<Member>();
		}

		public boolean accepts(ConstrainedElement constrainedElement) {

			if ( constrainedElement.getConstrainedElementKind() != ConstrainedElementKind.TYPE &&
					constrainedElement.getConstrainedElementKind() != ConstrainedElementKind.FIELD &&
					constrainedElement.getConstrainedElementKind() != ConstrainedElementKind.METHOD ) {
				return false;
			}

			if ( constrainedElement.getConstrainedElementKind() == ConstrainedElementKind.METHOD &&
					!( (ConstrainedMethod) constrainedElement ).isGetterMethod() ) {
				return false;
			}

			String propertyName1 = ReflectionHelper.getPropertyName( constrainedElement.getLocation().getMember() );
			String propertyName2 = ReflectionHelper.getPropertyName( root.getLocation().getMember() );

			return
					constrainedElement.getLocation()
							.getBeanClass()
							.isAssignableFrom( root.getLocation().getBeanClass() )
							&& ( ( propertyName1 != null && propertyName1.equals( propertyName2 ) ) ||
							propertyName1 == null && propertyName2 == null );
		}

		public void add(ConstrainedElement constrainedElement) {

			for ( MetaConstraint<?> oneConstraint : constrainedElement ) {
				constraints.add( oneConstraint );
			}

			if ( constrainedElement.isCascading() ) {
				cascadingMembers.add( constrainedElement.getLocation().getMember() );
			}
		}

		public PropertyMetaData build() {

			Set<MetaConstraint<?>> adaptedConstraints = newHashSet();

			for ( MetaConstraint<?> oneConstraint : constraints ) {
				adaptedConstraints.add(
						adaptOriginAndImplicitGroup(
								root.getLocation().getBeanClass(), oneConstraint
						)
				);
			}

			Member member = root.getLocation().getMember();
			return new PropertyMetaData(
					member != null ? member instanceof Field ? ( (Field) member ).getType() : ( (Method) member ).getReturnType() : null,
					member != null ? ReflectionHelper.getPropertyName( member ) : null,
					adaptedConstraints,
					cascadingMembers
			);
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
		private <A extends Annotation> MetaConstraint<A> adaptOriginAndImplicitGroup(Class<?> beanClass, MetaConstraint<A> constraint) {

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

			return new MetaConstraint<A>(
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
