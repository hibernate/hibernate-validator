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

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.metadata.constrained.ConstrainedElement;
import org.hibernate.validator.metadata.constrained.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.metadata.constrained.ConstrainedField;
import org.hibernate.validator.metadata.constrained.ConstrainedMethod;
import org.hibernate.validator.metadata.constrained.ConstrainedType;
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

	private PropertyMetaData(Class<?> type, String propertyName, Set<MetaConstraint<?>> constraints, Set<Member> cascadingMembers) {
		super(
				constraints,
				ConstraintMetaDataKind.PROPERTY,
				!cascadingMembers.isEmpty(),
				!cascadingMembers.isEmpty() || !constraints.isEmpty()
		);
		this.type = type;
		this.propertyName = propertyName;
		this.cascadingMembers = cascadingMembers;
	}

	public Class<?> getType() {
		return type;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Set<Member> getCascadingMembers() {
		return cascadingMembers;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
				+ propertyName + ", cascadingMembers=[" + cascadingMembers + "]]";
	}

	public static class Builder extends MetaDataBuilder {

		private final ConstrainedElement root;

		private final Set<MetaConstraint<?>> constraints;

		private final Set<Member> cascadingMembers;

		public Builder(ConstrainedField constrainedField, ConstraintHelper constraintHelper) {
			super( constraintHelper );

			this.root = constrainedField;
			this.constraints = newHashSet( constrainedField );
			this.cascadingMembers = constrainedField.isCascading() ? asSet(
					constrainedField.getLocation()
							.getMember()
			) : new HashSet<Member>();
		}

		public Builder(ConstrainedType constrainedType, ConstraintHelper constraintHelper) {
			super( constraintHelper );

			this.root = constrainedType;
			this.constraints = newHashSet( constrainedType );
			this.cascadingMembers = Collections.<Member>emptySet();
		}

		public Builder(ConstrainedMethod constrainedMethod, ConstraintHelper constraintHelper) {
			super( constraintHelper );

			this.root = constrainedMethod;
			this.constraints = newHashSet( constrainedMethod );
			this.cascadingMembers = constrainedMethod.isCascading() ? asSet(
					(Member) constrainedMethod.getLocation()
							.getMember()
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

			Set<MetaConstraint<?>> adaptedConstraints = adaptOriginsAndImplicitGroups(
					root.getLocation()
							.getBeanClass(), constraints
			);

			Member member = root.getLocation().getMember();
			return new PropertyMetaData(
					member != null ? member instanceof Field ? ( (Field) member ).getType() : ( (Method) member ).getReturnType() : null,
					member != null ? ReflectionHelper.getPropertyName( member ) : null,
					adaptedConstraints,
					cascadingMembers
			);
		}
	}

}
