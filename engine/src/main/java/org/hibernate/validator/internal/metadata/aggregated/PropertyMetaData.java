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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ElementKind;

import org.hibernate.validator.internal.engine.cascading.ValueExtractors;
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
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;

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

	private final Set<Cascadable> cascadables;

	private PropertyMetaData(String propertyName,
							 Type type,
							 Set<MetaConstraint<?>> constraints,
							 Set<Cascadable> cascadables) {
		super(
				propertyName,
				type,
				constraints,
				ElementKind.PROPERTY,
				!cascadables.isEmpty(),
				!cascadables.isEmpty() || !constraints.isEmpty()
		);

		this.cascadables = cascadables;
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
				// TODO which one to use ???
				cascadables.isEmpty() ? Collections.emptySet() : cascadables.iterator().next().getGroupConversionDescriptors()
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

		public Builder(Class<?> beanClass, ConstrainedField constrainedField, ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
				ValueExtractors valueExtractors) {
			super( beanClass, constraintHelper, typeResolutionHelper, valueExtractors );

			this.propertyName = constrainedField.getField().getName();
			this.propertyType = ReflectionHelper.typeOf( constrainedField.getField() );
			add( constrainedField );
		}

		public Builder(Class<?> beanClass, ConstrainedType constrainedType, ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
				ValueExtractors valueExtractors) {
			super( beanClass, constraintHelper, typeResolutionHelper, valueExtractors );

			this.propertyName = null;
			this.propertyType = null;
			add( constrainedType );
		}

		public Builder(Class<?> beanClass, ConstrainedExecutable constrainedMethod, ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
				ValueExtractors valueExtractors) {
			super( beanClass, constraintHelper, typeResolutionHelper, valueExtractors );

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

			if ( constrainedElement.isCascading() || !constrainedElement.getGroupConversions().isEmpty() ) {
				if ( constrainedElement.getKind() == ConstrainedElementKind.FIELD ) {
					Field field = ( (ConstrainedField) constrainedElement ).getField();
					Cascadable.Builder builder = cascadableBuilders.get( field );

					if ( builder == null ) {
						builder = new FieldCascadable.Builder( field );
						cascadableBuilders.put( field, builder );
					}

					builder.addGroupConversions( constrainedElement.getGroupConversions() );
					builder.addCascadingTypeParameters( constrainedElement.getCascadingTypeParameters() );
				}
				else if ( constrainedElement.getKind() == ConstrainedElementKind.METHOD ) {
					Method method = (Method) ( (ConstrainedExecutable) constrainedElement ).getExecutable();
					Cascadable.Builder builder = cascadableBuilders.get( method );

					if ( builder == null ) {
						builder = new GetterCascadable.Builder( method );
						cascadableBuilders.put( method, builder );
					}

					builder.addGroupConversions( constrainedElement.getGroupConversions() );
					builder.addCascadingTypeParameters( constrainedElement.getCascadingTypeParameters() );
				}
			}
		}

		@Override
		protected Set<MetaConstraint<?>> adaptConstraints(ConstrainedElementKind kind, Set<MetaConstraint<?>> constraints) {
			if ( kind == ConstrainedElementKind.FIELD || kind == ConstrainedElementKind.TYPE ) {
				return constraints;
			}

			// convert (getter) return value locations into property locations for usage within this meta-data
			return constraints.stream()
				.map( this::withAdaptedLocation )
				.collect( Collectors.toSet() );
		}

		private MetaConstraint<?> withAdaptedLocation(MetaConstraint<?> constraint) {
			ConstraintLocation adaptedLocation;

			if ( constraint.getLocation() instanceof TypeArgumentConstraintLocation ) {
				ConstraintLocation adaptedDelegate = ConstraintLocation.forProperty( constraint.getLocation().getMember() );
				adaptedLocation = ConstraintLocation.forTypeArgument( adaptedDelegate,
						( (TypeArgumentConstraintLocation) constraint.getLocation() ).getTypeParameter(),
						constraint.getLocation().getTypeForValidatorResolution()
				);
			}
			else {
				adaptedLocation = ConstraintLocation.forProperty( constraint.getLocation().getMember() );
			}

			return MetaConstraints.create( typeResolutionHelper, valueExtractors, constraint.getDescriptor(), adaptedLocation );
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
					cascadables
			);
		}
	}
}
