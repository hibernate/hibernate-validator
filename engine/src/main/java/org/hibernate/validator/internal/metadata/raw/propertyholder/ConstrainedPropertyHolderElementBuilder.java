/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw.propertyholder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.aggregated.cascading.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.core.MetaConstraintBuilder;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.properties.propertyholder.PropertyAccessorCreatorProvider;
import org.hibernate.validator.internal.properties.propertyholder.PropertyHolderProperty;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * @author Marko Bekhta
 */
public abstract class ConstrainedPropertyHolderElementBuilder {

	protected final ConfigurationSource source;

	protected final String name;

	@Immutable
	protected final Set<MetaConstraintBuilder<?>> constraints;
	protected final CascadingMetaDataBuilder cascadingMetaDataBuilder;
	@Immutable
	protected final Set<MetaConstraintBuilder<?>> typeArgumentConstraints;

	public ConstrainedPropertyHolderElementBuilder(ConfigurationSource source,
			String name, Set<MetaConstraintBuilder<?>> constraints,
			Set<MetaConstraintBuilder<?>> typeArgumentConstraints,
			CascadingMetaDataBuilder cascadingMetaDataBuilder) {
		this.source = source;
		this.name = name;
		this.constraints = CollectionHelper.toImmutableSetOfNullable( constraints );
		this.typeArgumentConstraints = CollectionHelper.toImmutableSetOfNullable( typeArgumentConstraints );
		this.cascadingMetaDataBuilder = cascadingMetaDataBuilder;
	}

	public boolean isConstrained() {
		return cascadingMetaDataBuilder.isMarkedForCascadingOnAnnotatedObjectOrContainerElements()
				|| cascadingMetaDataBuilder.hasGroupConversionsOnAnnotatedObjectOrContainerElements()
				|| !constraints.isEmpty()
				|| !typeArgumentConstraints.isEmpty();
	}

	public ConstrainedElement build(TypeResolutionHelper typeResolutionHelper, ConstraintHelper constraintHelper, ValueExtractorManager valueExtractorManager, PropertyAccessorCreatorProvider propertyAccessorCreatorProvider, Class<?> propertyHolderType) {
		PropertyHolderProperty property = createPropertyHolderProperty( propertyAccessorCreatorProvider, propertyHolderType );

		return new ConstrainedField(
				source,
				property,
				toMetaConstraints( typeResolutionHelper, constraintHelper, valueExtractorManager, property, constraints ),
				toMetaConstraints( typeResolutionHelper, constraintHelper, valueExtractorManager, property, typeArgumentConstraints ),
				cascadingMetaDataBuilder
		);
	}

	protected abstract PropertyHolderProperty createPropertyHolderProperty(PropertyAccessorCreatorProvider propertyAccessorCreatorProvider, Class<?> propertyHolderType);

	protected Set<MetaConstraint<?>> toMetaConstraints(TypeResolutionHelper typeResolutionHelper, ConstraintHelper constraintHelper, ValueExtractorManager valueExtractorManager, PropertyHolderProperty property, Collection<MetaConstraintBuilder<?>> collection) {
		Set<MetaConstraint<?>> builtConstraints = new HashSet<>( constraints.size() );
		for ( MetaConstraintBuilder<?> builder : constraints ) {
			builtConstraints.add( builder.build( typeResolutionHelper, constraintHelper, valueExtractorManager, property ) );
		}
		return builtConstraints;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder( getClass().getSimpleName() );
		sb.append( "{" );
		sb.append( "source=" ).append( source );
		sb.append( ", name='" ).append( name ).append( '\'' );
		sb.append( ", constraints=" ).append( constraints );
		sb.append( ", cascadingMetaDataBuilder=" ).append( cascadingMetaDataBuilder );
		sb.append( ", typeArgumentConstraints=" ).append( typeArgumentConstraints );
		sb.append( '}' );
		return sb.toString();
	}


	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ConstrainedPropertyHolderElementBuilder that = (ConstrainedPropertyHolderElementBuilder) o;

		if ( source != that.source ) {
			return false;
		}
		if ( !name.equals( that.name ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = source.hashCode();
		result = 31 * result + name.hashCode();
		return result;
	}
}
