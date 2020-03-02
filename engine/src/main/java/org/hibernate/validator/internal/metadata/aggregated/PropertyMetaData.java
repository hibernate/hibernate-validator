/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.invoke.MethodHandles;
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

import jakarta.validation.ElementKind;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.core.MetaConstraints;
import org.hibernate.validator.internal.metadata.descriptor.PropertyDescriptorImpl;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.GetterConstraintLocation;
import org.hibernate.validator.internal.metadata.location.TypeArgumentConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.properties.Getter;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.properties.javabean.JavaBeanGetter;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
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
 * @author Guillaume Smet
 */
public class PropertyMetaData extends AbstractConstraintMetaData {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	@Immutable
	private final Set<Cascadable> cascadables;

	private PropertyMetaData(String propertyName,
							 Type type,
							 Set<MetaConstraint<?>> constraints,
							 Set<MetaConstraint<?>> containerElementsConstraints,
							 Set<Cascadable> cascadables) {
		super(
				propertyName,
				type,
				constraints,
				containerElementsConstraints,
				!cascadables.isEmpty(),
				!cascadables.isEmpty() || !constraints.isEmpty() || !containerElementsConstraints.isEmpty()
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
	public ElementKind getKind() {
		return ElementKind.PROPERTY;
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
				ConstrainedElementKind.FIELD,
				ConstrainedElementKind.GETTER
		);

		private final String propertyName;
		private final Map<Property, Cascadable.Builder> cascadableBuilders = new HashMap<>();
		private final Type propertyType;

		public Builder(Class<?> beanClass, ConstrainedField constrainedProperty, ConstraintCreationContext constraintCreationContext) {
			super( beanClass, constraintCreationContext );

			this.propertyName = constrainedProperty.getField().getName();
			this.propertyType = constrainedProperty.getField().getType();
			add( constrainedProperty );
		}

		public Builder(Class<?> beanClass, ConstrainedExecutable constrainedMethod, ConstraintCreationContext constraintCreationContext) {
			super( beanClass, constraintCreationContext );

			this.propertyName = constrainedMethod.getCallable().as( Property.class ).getPropertyName();
			this.propertyType = constrainedMethod.getCallable().getType();
			add( constrainedMethod );
		}

		@Override
		public boolean accepts(ConstrainedElement constrainedElement) {
			if ( !SUPPORTED_ELEMENT_KINDS.contains( constrainedElement.getKind() ) ) {
				return false;
			}

			return Objects.equals( getPropertyName( constrainedElement ), propertyName );
		}

		@Override
		public final void add(ConstrainedElement constrainedElement) {
			super.add( constrainedElement );

			if ( constrainedElement.getCascadingMetaDataBuilder().isMarkedForCascadingOnAnnotatedObjectOrContainerElements() ||
					constrainedElement.getCascadingMetaDataBuilder().hasGroupConversionsOnAnnotatedObjectOrContainerElements() ) {

				Property property = getConstrainableFromConstrainedElement( constrainedElement );

				Cascadable.Builder builder = cascadableBuilders.get( property );
				if ( builder == null ) {
					builder = AbstractPropertyCascadable.AbstractBuilder.builder(
							constraintCreationContext.getValueExtractorManager(), property,
							constrainedElement.getCascadingMetaDataBuilder() );
					cascadableBuilders.put( property, builder );
				}
				else {
					builder.mergeCascadingMetaData( constrainedElement.getCascadingMetaDataBuilder() );
				}
			}
		}

		private Property getConstrainableFromConstrainedElement(ConstrainedElement constrainedElement) {
			switch ( constrainedElement.getKind() ) {
				case FIELD:
					if ( constrainedElement instanceof ConstrainedField ) {
						return ( (ConstrainedField) constrainedElement ).getField();
					}
					else {
						throw LOG.getUnexpectedConstraintElementType( ConstrainedField.class, constrainedElement.getClass() );
					}
				case GETTER:
					if ( constrainedElement instanceof ConstrainedExecutable ) {
						return ( (ConstrainedExecutable) constrainedElement ).getCallable().as( Getter.class );
					}
					else {
						throw LOG.getUnexpectedConstraintElementType( ConstrainedExecutable.class, constrainedElement.getClass() );
					}
				default:
					throw LOG.getUnsupportedConstraintElementType( constrainedElement.getKind() );
			}
		}

		@Override
		protected Set<MetaConstraint<?>> adaptConstraints(ConstrainedElement constrainedElement, Set<MetaConstraint<?>> constraints) {
			if ( constraints.isEmpty() || constrainedElement.getKind() != ConstrainedElementKind.GETTER ) {
				return constraints;
			}

			ConstraintLocation getterConstraintLocation = ConstraintLocation
					.forGetter( ( (ConstrainedExecutable) constrainedElement ).getCallable().as( JavaBeanGetter.class ) );

			// convert return value locations into getter locations for usage within this meta-data
			return constraints.stream()
					.map( c -> withGetterLocation( getterConstraintLocation, c ) )
					.collect( Collectors.toSet() );
		}

		private MetaConstraint<?> withGetterLocation(ConstraintLocation getterConstraintLocation, MetaConstraint<?> constraint) {
			ConstraintLocation converted = null;

			// fast track if it's a regular constraint
			if ( !( constraint.getLocation() instanceof TypeArgumentConstraintLocation ) ) {
				// Change the constraint location to a GetterConstraintLocation if it is not already one
				if ( constraint.getLocation() instanceof GetterConstraintLocation ) {
					converted = constraint.getLocation();
				}
				else {
					converted = getterConstraintLocation;
				}
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
						// Change the constraint location to a GetterConstraintLocation if it is not already one
						if ( location instanceof GetterConstraintLocation ) {
							converted = location;
						}
						else {
							converted = getterConstraintLocation;
						}
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

			return MetaConstraints.create( constraintCreationContext.getTypeResolutionHelper(),
					constraintCreationContext.getValueExtractorManager(),
					constraintCreationContext.getConstraintValidatorManager(), constraint.getDescriptor(), converted );
		}

		private String getPropertyName(ConstrainedElement constrainedElement) {
			if ( constrainedElement.getKind() == ConstrainedElementKind.FIELD ) {
				return ( (ConstrainedField) constrainedElement ).getField().getPropertyName();
			}
			else if ( constrainedElement.getKind() == ConstrainedElementKind.GETTER ) {
				return ( (ConstrainedExecutable) constrainedElement ).getCallable().as( Property.class ).getPropertyName();
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
					adaptOriginsAndImplicitGroups( getDirectConstraints() ),
					adaptOriginsAndImplicitGroups( getContainerElementConstraints() ),
					cascadables
			);
		}
	}
}
