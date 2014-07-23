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
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ElementKind;
import javax.validation.metadata.GroupConversionDescriptor;

import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.PropertyDescriptorImpl;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * Represents the constraint related meta data for a JavaBeans property.
 * Abstracts from the concrete physical type of the underlying Java element(s)
 * (fields or getter methods).
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
public class PropertyMetaData extends AbstractConstraintMetaData implements Cascadable {

	/**
	 * The member marked as cascaded (either field or getter). Used to retrieve
	 * this property's value during cascaded validation.
	 */
	private final Member cascadingMember;

	private final ElementType elementType;

	private final GroupConversionHelper groupConversionHelper;

	private PropertyMetaData(String propertyName,
							 Type type,
							 Set<MetaConstraint<?>> constraints,
							 Map<Class<?>, Class<?>> groupConversions,
							 Member cascadingMember,
							 boolean requiresUnwrapping) {
		super(
				propertyName,
				type,
				constraints,
				ElementKind.PROPERTY,
				cascadingMember != null,
				cascadingMember != null || !constraints.isEmpty(),
				requiresUnwrapping
		);

		if ( cascadingMember != null ) {
			this.cascadingMember = cascadingMember;
			this.elementType = cascadingMember instanceof Field ? ElementType.FIELD : ElementType.METHOD;
		}
		else {
			this.cascadingMember = null;
			this.elementType = ElementType.TYPE;
		}

		this.groupConversionHelper = new GroupConversionHelper( groupConversions );
		this.groupConversionHelper.validateGroupConversions( isCascading(), this.toString() );
	}

	public Member getCascadingMember() {
		return cascadingMember;
	}

	@Override
	public ElementType getElementType() {
		return elementType;
	}

	@Override
	public Class<?> convertGroup(Class<?> from) {
		return groupConversionHelper.convertGroup( from );
	}

	@Override
	public Set<GroupConversionDescriptor> getGroupConversionDescriptors() {
		return groupConversionHelper.asDescriptors();
	}

	@Override
	public PropertyDescriptorImpl asDescriptor(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		return new PropertyDescriptorImpl(
				getType(),
				getName(),
				asDescriptors( getConstraints() ),
				isCascading(),
				defaultGroupSequenceRedefined,
				defaultGroupSequence,
				getGroupConversionDescriptors()
		);
	}

	@Override
	public String toString() {

		return "PropertyMetaData [type=" + getType() + ", propertyName="
				+ getName() + ", cascadingMember=[" + cascadingMember + "]]";
	}

	@Override
	public int hashCode() {
		return super.hashCode();
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
		return true;
	}

	public static class Builder extends MetaDataBuilder {

		private static final EnumSet<ConstrainedElementKind> SUPPORTED_ELEMENT_KINDS = EnumSet.of(
				ConstrainedElementKind.TYPE,
				ConstrainedElementKind.FIELD,
				ConstrainedElementKind.METHOD
		);

		private final String propertyName;
		private final Type propertyType;
		private Member cascadingMember;

		public Builder(Class<?> beanClass, ConstrainedField constrainedField, ConstraintHelper constraintHelper) {
			super( beanClass, constraintHelper );

			this.propertyName = ReflectionHelper.getPropertyName( constrainedField.getLocation().getMember() );
			this.propertyType = ReflectionHelper.typeOf( constrainedField.getLocation().getMember() );
			add( constrainedField );
		}

		public Builder(Class<?> beanClass, ConstrainedType constrainedType, ConstraintHelper constraintHelper) {
			super( beanClass, constraintHelper );

			this.propertyName = null;
			this.propertyType = null;
			add( constrainedType );
		}

		public Builder(Class<?> beanClass, ConstrainedExecutable constrainedMethod, ConstraintHelper constraintHelper) {
			super( beanClass, constraintHelper );

			this.propertyName = ReflectionHelper.getPropertyName( constrainedMethod.getLocation().getMember() );
			this.propertyType = ReflectionHelper.typeOf( constrainedMethod.getLocation().getMember() );
			add( constrainedMethod );
		}

		@Override
		public boolean accepts(ConstrainedElement constrainedElement) {
			if ( !SUPPORTED_ELEMENT_KINDS.contains( constrainedElement.getKind() ) ) {
				return false;
			}

			if ( constrainedElement.getKind() == ConstrainedElementKind.METHOD &&
					!( (ConstrainedExecutable) constrainedElement ).isGetterMethod() ) {
				return false;
			}

			return equals(
					ReflectionHelper.getPropertyName( constrainedElement.getLocation().getMember() ),
					propertyName
			);
		}

		@Override
		public void add(ConstrainedElement constrainedElement) {

			super.add( constrainedElement );

			if ( constrainedElement.isCascading() && cascadingMember == null ) {
				cascadingMember = constrainedElement.getLocation().getMember();
			}
		}

		@Override
		public PropertyMetaData build() {
			return new PropertyMetaData(
					propertyName,
					propertyType,
					adaptOriginsAndImplicitGroups( getConstraints() ),
					getGroupConversions(),
					cascadingMember,
					requiresUnwrapping()
			);
		}

		private boolean equals(String s1, String s2) {
			return ( s1 != null && s1.equals( s2 ) ) || ( s1 == null && s2 == null );
		}
	}
}
