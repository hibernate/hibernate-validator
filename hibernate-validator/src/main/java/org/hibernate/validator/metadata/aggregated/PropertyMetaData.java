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
package org.hibernate.validator.metadata.aggregated;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.EnumSet;
import java.util.Set;

import org.hibernate.validator.metadata.core.MetaConstraint;
import org.hibernate.validator.metadata.core.ConstraintHelper;
import org.hibernate.validator.metadata.raw.ConstrainedElement;
import org.hibernate.validator.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.metadata.raw.ConstrainedField;
import org.hibernate.validator.metadata.raw.ConstrainedMethod;
import org.hibernate.validator.metadata.raw.ConstrainedType;
import org.hibernate.validator.util.ReflectionHelper;

import static org.hibernate.validator.util.CollectionHelper.newHashSet;

/**
 * <p>
 * Represents the constraint related meta data for a JavaBeans property.
 * Abstracts from the concrete physical type of the underlying Java element(s)
 * (fields or getter methods).
 * </p>
 * <p>
 * In order to provide a unified access to all JavaBeans constraints also
 * class-level constraints are represented by this meta data type.
 * </p>
 * <p>
 * Identity is solely based on the property name, hence sets and similar
 * collections of this type may only be created in the scope of one Java type.
 * </p>
 *
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
		int result = 1;
		result = prime * result
				+ ( ( propertyName == null ) ? 0 : propertyName.hashCode() );
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
		PropertyMetaData other = (PropertyMetaData) obj;
		if ( propertyName == null ) {
			if ( other.propertyName != null ) {
				return false;
			}
		}
		else if ( !propertyName.equals( other.propertyName ) ) {
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

		private final static EnumSet<ConstrainedElementKind> SUPPORTED_ELEMENT_KINDS = EnumSet.of(
				ConstrainedElementKind.TYPE,
				ConstrainedElementKind.FIELD,
				ConstrainedElementKind.METHOD
		);

		private final Class<?> beanClass;

		private final String propertyName;

		private final Class<?> propertyType;

		private final Set<MetaConstraint<?>> constraints = newHashSet();

		private final Set<Member> cascadingMembers = newHashSet();

		public Builder(ConstrainedField constrainedField, ConstraintHelper constraintHelper) {
			super( constraintHelper );

			this.beanClass = constrainedField.getLocation().getBeanClass();
			this.propertyName = ReflectionHelper.getPropertyName( constrainedField.getLocation().getMember() );
			this.propertyType = ( (Field) constrainedField.getLocation().getMember() ).getType();
			add( constrainedField );
		}

		public Builder(ConstrainedType constrainedType, ConstraintHelper constraintHelper) {
			super( constraintHelper );

			this.beanClass = constrainedType.getLocation().getBeanClass();
			this.propertyName = null;
			this.propertyType = null;
			add( constrainedType );
		}

		public Builder(ConstrainedMethod constrainedMethod, ConstraintHelper constraintHelper) {
			super( constraintHelper );

			this.beanClass = constrainedMethod.getLocation().getBeanClass();
			this.propertyName = ReflectionHelper.getPropertyName( constrainedMethod.getLocation().getMember() );
			this.propertyType = constrainedMethod.getLocation().getMember().getReturnType();
			add( constrainedMethod );
		}

		public boolean accepts(ConstrainedElement constrainedElement) {

			if ( !SUPPORTED_ELEMENT_KINDS.contains( constrainedElement.getConstrainedElementKind() ) ) {
				return false;
			}

			if ( constrainedElement.getConstrainedElementKind() == ConstrainedElementKind.METHOD &&
					!( (ConstrainedMethod) constrainedElement ).isGetterMethod() ) {
				return false;
			}

			return equals(
					ReflectionHelper.getPropertyName( constrainedElement.getLocation().getMember() ),
					propertyName
			);
		}

		public void add(ConstrainedElement constrainedElement) {

			constraints.addAll( constrainedElement.getConstraints() );

			if ( constrainedElement.isCascading() ) {
				cascadingMembers.add( constrainedElement.getLocation().getMember() );
			}
		}

		public PropertyMetaData build() {

			return new PropertyMetaData(
					propertyType,
					propertyName,
					adaptOriginsAndImplicitGroups( beanClass, constraints ),
					cascadingMembers
			);
		}

		private boolean equals(String s1, String s2) {
			return ( s1 != null && s1.equals( s2 ) ) || ( s1 == null && s2 == null );
		}
	}

}
