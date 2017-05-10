/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ElementKind;

import org.hibernate.validator.internal.engine.cascading.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.core.MetaConstraints;
import org.hibernate.validator.internal.metadata.descriptor.PropertyDescriptorImpl;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.TypeArgumentConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

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

	@Immutable
	private final Set<Cascadable> cascadables;

	private PropertyMetaData(String propertyName,
							 Type type,
							 Set<MetaConstraint<?>> constraints,
							 Set<MetaConstraint<?>> containerElementsConstraints,
							 Set<Cascadable> cascadables,
							 boolean cascadingProperty) {
		super(
				propertyName,
				type,
				constraints,
				containerElementsConstraints,
				ElementKind.PROPERTY,
				!cascadables.isEmpty(),
				!cascadables.isEmpty() || !constraints.isEmpty()
		);

		this.cascadables = CollectionHelper.toImmutableSet( cascadables );
	}

	@Override
	public PropertyDescriptorImpl asDescriptor(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		// TODO we have one CascadingMetaData per Cascadable but we need only one to provide a view to the
		// Bean Validation metadata API so we pick the first one...
		CascadingMetaData firstCascadingMetaData = cascadables.isEmpty() ? null : cascadables.iterator().next().getCascadingMetaData();

		return new PropertyDescriptorImpl(
				getType(),
				getName(),
				asDescriptors( getDirectConstraints() ),
				asContainerElementTypeDescriptors( getContainerElementsConstraints(), firstCascadingMetaData, defaultGroupSequenceRedefined, defaultGroupSequence ),
				firstCascadingMetaData != null ? firstCascadingMetaData.isCascading() : false,
				defaultGroupSequenceRedefined,
				defaultGroupSequence,
				firstCascadingMetaData != null ? firstCascadingMetaData.getGroupConversionDescriptors() : Collections.emptySet()
		);
	}

	/**
	 * Returns the cascadables of this property, if any. Often, there will be just a single element returned. Several
	 * elements may be returned in the following cases:
	 * <ul>
	 * <li>a property's field has been marked with {@code @Valid} but type-level constraints have been given on the
	 * getter</li>
	 * <li>one type parameter of a property has been marked with {@code @Valid} on the field (e.g. a map's key) but
	 * another type parameter has been marked with {@code @Valid} on the property (e.g. the map's value)</li>
	 * <li>a (shaded) private field in a super-type and another field of the same name in a sub-type are both marked
	 * with {@code @Valid}</li>
	 * </ul>
	 */
	public Set<Cascadable> getCascadables() {
		return cascadables;
	}

	@Override
	public String toString() {
		return "PropertyMetaData [type=" + getType() + ", propertyName=" + getName() + "]]";
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
		private final Map<Member, Cascadable.Builder> cascadableBuilders = new HashMap<>();
		private final Type propertyType;
		private boolean cascadingProperty = false;

		public Builder(Class<?> beanClass, ConstrainedField constrainedField, ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
				ValueExtractorManager valueExtractorManager) {
			super( beanClass, constraintHelper, typeResolutionHelper, valueExtractorManager );

			this.propertyName = constrainedField.getField().getName();
			this.propertyType = ReflectionHelper.typeOf( constrainedField.getField() );
			add( constrainedField );
		}

		public Builder(Class<?> beanClass, ConstrainedType constrainedType, ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
				ValueExtractorManager valueExtractorManager) {
			super( beanClass, constraintHelper, typeResolutionHelper, valueExtractorManager );

			this.propertyName = null;
			this.propertyType = null;
			add( constrainedType );
		}

		public Builder(Class<?> beanClass, ConstrainedExecutable constrainedMethod, ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
				ValueExtractorManager valueExtractorManager) {
			super( beanClass, constraintHelper, typeResolutionHelper, valueExtractorManager );

			this.propertyName = ReflectionHelper.getPropertyName( constrainedMethod.getExecutable() );
			this.propertyType = ReflectionHelper.typeOf( constrainedMethod.getExecutable() );
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

			return Objects.equals( getPropertyName( constrainedElement ), propertyName );
		}

		@Override
		public final void add(ConstrainedElement constrainedElement) {
			super.add( constrainedElement );

			cascadingProperty = cascadingProperty || constrainedElement.getCascadingMetaData().isCascading();

			if ( constrainedElement.getCascadingMetaData().isMarkedForCascadingOnElementOrContainerElements() ||
					constrainedElement.getCascadingMetaData().hasGroupConversionsOnElementOrContainerElements() ) {
				if ( constrainedElement.getKind() == ConstrainedElementKind.FIELD ) {
					Field field = ( (ConstrainedField) constrainedElement ).getField();
					Cascadable.Builder builder = cascadableBuilders.get( field );

					if ( builder == null ) {
						builder = new FieldCascadable.Builder( field, constrainedElement.getCascadingMetaData() );
						cascadableBuilders.put( field, builder );
					}
					else {
						builder.mergeCascadingMetaData( constrainedElement.getCascadingMetaData() );
					}
				}
				else if ( constrainedElement.getKind() == ConstrainedElementKind.METHOD ) {
					Method method = (Method) ( (ConstrainedExecutable) constrainedElement ).getExecutable();
					Cascadable.Builder builder = cascadableBuilders.get( method );

					if ( builder == null ) {
						builder = new GetterCascadable.Builder( method, constrainedElement.getCascadingMetaData() );
						cascadableBuilders.put( method, builder );
					}
					else {
						builder.mergeCascadingMetaData( constrainedElement.getCascadingMetaData() );
					}
				}
			}
		}

		@Override
		protected Set<MetaConstraint<?>> adaptConstraints(ConstrainedElementKind kind, Set<MetaConstraint<?>> constraints) {
			if ( kind == ConstrainedElementKind.FIELD || kind == ConstrainedElementKind.TYPE ) {
				return constraints;
			}

			// convert return value locations into getter locations for usage within this meta-data
			return constraints.stream()
				.map( this::withGetterLocation )
				.collect( Collectors.toSet() );
		}

		private MetaConstraint<?> withGetterLocation(MetaConstraint<?> constraint) {
			ConstraintLocation converted = null;

			// fast track if it's a regular constraint
			if ( !(constraint.getLocation() instanceof TypeArgumentConstraintLocation) ) {
				converted = ConstraintLocation.forGetter( (Method) constraint.getLocation().getMember() );
			}
			else {
				Deque<ConstraintLocation> locationStack = new ArrayDeque<>();

				// 1. collect the hierarchy of delegates up to the root return value location
				ConstraintLocation current = constraint.getLocation();
				do {
					locationStack.addFirst( current );
					if ( current instanceof TypeArgumentConstraintLocation ) {
						current = ( (TypeArgumentConstraintLocation) current ).getDelegate();
					}
					else {
						current = null;
					}
				}
				while ( current != null );

				// 2. beginning at the root, transform each location so it references the transformed delegate
				for ( ConstraintLocation location : locationStack ) {
					if ( !(location instanceof TypeArgumentConstraintLocation) ) {
						converted = ConstraintLocation.forGetter( (Method) location.getMember() );
					}
					else {
						converted = ConstraintLocation.forTypeArgument(
							converted,
							( (TypeArgumentConstraintLocation) location ).getTypeParameter(),
							location.getTypeForValidatorResolution()
						);
					}
				}
			}

			return MetaConstraints.create( typeResolutionHelper, valueExtractorManager, constraint.getDescriptor(), converted );
		}

		private String getPropertyName(ConstrainedElement constrainedElement) {
			if ( constrainedElement.getKind() == ConstrainedElementKind.FIELD ) {
				return ReflectionHelper.getPropertyName( ( (ConstrainedField) constrainedElement ).getField() );
			}
			else if ( constrainedElement.getKind() == ConstrainedElementKind.METHOD ) {
				return ReflectionHelper.getPropertyName( ( (ConstrainedExecutable) constrainedElement ).getExecutable() );
			}

			return null;
		}

		@Override
		public PropertyMetaData build() {
			Set<Cascadable> cascadables = cascadableBuilders.values()
					.stream()
					.map( b -> b.build() )
					.collect( Collectors.toSet() );

			return new PropertyMetaData(
					propertyName,
					propertyType,
					adaptOriginsAndImplicitGroups( getConstraints() ),
					adaptOriginsAndImplicitGroups( getContainerElementConstraints() ),
					cascadables,
					cascadingProperty
			);
		}
	}
}
