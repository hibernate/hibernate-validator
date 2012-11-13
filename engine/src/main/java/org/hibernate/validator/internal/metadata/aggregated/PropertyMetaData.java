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

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.PropertyDescriptorImpl;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedMethod;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.util.ReflectionHelper;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

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
public class PropertyMetaData extends AbstractConstraintMetaData {

	/**
	 * Access strategy when retrieving the value of this property.
	 */
	public enum ValueAccessStrategy {
		FIELD, GETTER;
	}

	/**
	 * The member marked as cascaded (either field or getter). Used to retrieve
	 * this property's value during cascaded validation.
	 */
	private final Member cascadingMember;

	private ValueAccessStrategy cascadingValueAccessStrategy;

	private PropertyMetaData(String propertyName,
							 Type type,
							 Set<MetaConstraint<?>> constraints,
							 Member cascadingMember) {
		super(
				propertyName,
				type,
				constraints,
				ConstraintMetaDataKind.PROPERTY,
				cascadingMember != null,
				cascadingMember != null || !constraints.isEmpty()
		);

		if ( cascadingMember != null ) {
			this.cascadingMember = cascadingMember;
			this.cascadingValueAccessStrategy = cascadingMember instanceof Field ? ValueAccessStrategy.FIELD : ValueAccessStrategy.GETTER;
		}
		else {
			this.cascadingMember = null;
			this.cascadingValueAccessStrategy = null;
		}
	}

	/**
	 * Retrieves the value of this property from the given object in case this
	 * property.
	 *
	 * @param o
	 *            The object to retrieve the value from.
	 * @param accessStrategy
	 *            The strategy to use for retrieving the value.
	 *
	 * @return This property's value.
	 */
	public Object getValue(Object o, ValueAccessStrategy accessStrategy) {

		if ( accessStrategy == ValueAccessStrategy.FIELD ) {
			return ReflectionHelper.getValue( (Field) cascadingMember, o );
		}
		else if ( accessStrategy == ValueAccessStrategy.GETTER ) {
			return ReflectionHelper.getValue( (Method) cascadingMember, o );
		}
		else {
			return null;
		}
	}

	public ValueAccessStrategy getCascadedValueAccessStrategy() {
		return cascadingValueAccessStrategy;
	}

	public Class<?> getRawType() {
		return ReflectionHelper.getType( cascadingMember );
	}

	@Override
	public PropertyDescriptorImpl asDescriptor(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		return new PropertyDescriptorImpl(
				getType(),
				getName(),
				asDescriptors( getConstraints() ),
				isCascading(),
				defaultGroupSequenceRedefined,
				defaultGroupSequence
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

		private final static EnumSet<ConstrainedElementKind> SUPPORTED_ELEMENT_KINDS = EnumSet.of(
				ConstrainedElementKind.TYPE,
				ConstrainedElementKind.FIELD,
				ConstrainedElementKind.METHOD
		);

		private final Class<?> beanClass;
		private final String propertyName;
		private final Type propertyType;
		private final Set<MetaConstraint<?>> constraints = newHashSet();
		private Member cascadingMember;


		public Builder(ConstrainedField constrainedField, ConstraintHelper constraintHelper) {
			super( constraintHelper );

			this.beanClass = constrainedField.getLocation().getBeanClass();
			this.propertyName = ReflectionHelper.getPropertyName( constrainedField.getLocation().getMember() );
			this.propertyType = ( (Field) constrainedField.getLocation().getMember() ).getGenericType();
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
			this.propertyType = constrainedMethod.getLocation().typeOfAnnotatedElement();
			add( constrainedMethod );
		}

		@Override
		public boolean accepts(ConstrainedElement constrainedElement) {
			if ( !SUPPORTED_ELEMENT_KINDS.contains( constrainedElement.getKind() ) ) {
				return false;
			}

			if ( constrainedElement.getKind() == ConstrainedElementKind.METHOD &&
					!( (ConstrainedMethod) constrainedElement ).isGetterMethod() ) {
				return false;
			}

			return equals(
					ReflectionHelper.getPropertyName( constrainedElement.getLocation().getMember() ),
					propertyName
			);
		}

		@Override
		public void add(ConstrainedElement constrainedElement) {
			constraints.addAll( constrainedElement.getConstraints() );

			if ( constrainedElement.isCascading() && cascadingMember == null ) {
				cascadingMember = constrainedElement.getLocation().getMember();
			}
		}

		@Override
		public PropertyMetaData build() {
			return new PropertyMetaData(
					propertyName,
					propertyType,
					adaptOriginsAndImplicitGroups( beanClass, constraints ),
					cascadingMember
			);
		}

		private boolean equals(String s1, String s2) {
			return ( s1 != null && s1.equals( s2 ) ) || ( s1 == null && s2 == null );
		}
	}
}
