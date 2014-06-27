/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
import org.hibernate.validator.internal.metadata.descriptor.TypeArgumentDescriptorImpl;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedTypeArgument;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * @author Khalid Alqinyah
 */
public class TypeArgumentMetaData extends AbstractConstraintMetaData implements Cascadable {

	/**
	 * The member marked as cascaded (either field or getter). Used to retrieve
	 * this property's value during cascaded validation.
	 */
	private final Member cascadingMember;

	private final ElementType elementType;

	private final GroupConversionHelper groupConversionHelper;

	private TypeArgumentMetaData(String propertyName,
							 Type type,
							 Set<MetaConstraint<?>> constraints,
							 Map<Class<?>, Class<?>> groupConversions,
							 Member cascadingMember,
							 boolean requiresUnwrapping) {
		super(
				propertyName,
				type,
				constraints,
				ElementKind.PARAMETER,
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
			this.elementType = ElementType.TYPE_USE;
		}

		this.groupConversionHelper = new GroupConversionHelper( groupConversions );
		this.groupConversionHelper.validateGroupConversions( isCascading(), this.toString() );
	}

	@Override
	public Object getValue(Object parent) {
		return ReflectionHelper.getValue( cascadingMember, parent );
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
	public TypeArgumentDescriptorImpl asDescriptor(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		return new TypeArgumentDescriptorImpl(
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

		return "TypeArgumentMetaData [type=" + getType() + ", propertyName="
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

		private static final EnumSet<ConstrainedElement.ConstrainedElementKind> SUPPORTED_ELEMENT_KINDS = EnumSet.of(
				ConstrainedElement.ConstrainedElementKind.TYPE_USE
		);

		private final String propertyName;
		private final Type propertyType;
		private Member cascadingMember;

		public Builder(Class<?> beanClass, ConstrainedTypeArgument constrainedTypeArgument, ConstraintHelper constraintHelper) {
			super( beanClass, constraintHelper );

			this.propertyName = ReflectionHelper.getPropertyName( constrainedTypeArgument.getLocation().getMember() );
			this.propertyType = constrainedTypeArgument.getLocation().getTypeForValidatorResolution();
			add( constrainedTypeArgument );
		}

		@Override
		public boolean accepts(ConstrainedElement constrainedElement) {
			if ( !SUPPORTED_ELEMENT_KINDS.contains( constrainedElement.getKind() ) ) {
				return false;
			}

			if ( constrainedElement.getKind() == ConstrainedElement.ConstrainedElementKind.METHOD &&
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
		public TypeArgumentMetaData build() {
			return new TypeArgumentMetaData(
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
