/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw.propertyholder;

import java.util.Set;

import org.hibernate.validator.internal.metadata.aggregated.cascading.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.MetaConstraintBuilder;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.properties.PropertyAccessor;
import org.hibernate.validator.internal.properties.propertyholder.PropertyAccessorCreatorProvider;
import org.hibernate.validator.internal.properties.propertyholder.PropertyHolderProperty;
import org.hibernate.validator.spi.propertyholder.PropertyAccessorCreator;

/**
 * @author Marko Bekhta
 */
public class SimpleConstrainedPropertyHolderElementBuilder extends ConstrainedPropertyHolderElementBuilder {

	private final Class<?> type;

	public SimpleConstrainedPropertyHolderElementBuilder(ConfigurationSource source,
			String name, Class<?> type, Set<MetaConstraintBuilder<?>> constraints,
			Set<MetaConstraintBuilder<?>> typeArgumentConstraints, CascadingMetaDataBuilder cascadingMetaDataBuilder) {
		super( source, name, constraints, typeArgumentConstraints, cascadingMetaDataBuilder );
		this.type = type;
	}

	public SimpleConstrainedPropertyHolderElementBuilder(ConfigurationSource source,
			String name, Class<?> type, Set<MetaConstraintBuilder<?>> constraints,
			Set<MetaConstraintBuilder<?>> typeArgumentConstraints) {
		this( source, name, type, constraints, typeArgumentConstraints, CascadingMetaDataBuilder.nonCascading() );
	}

	@Override
	protected PropertyHolderProperty createPropertyHolderProperty(PropertyAccessorCreatorProvider propertyAccessorCreatorProvider, Class<?> propertyHolderType) {
		PropertyAccessorCreator<?> propertyAccessorCreator = propertyAccessorCreatorProvider.getPropertyAccessorCreatorFor( propertyHolderType );
		PropertyAccessor propertyAccessor = propertyAccessorCreator.create( name, type );

		return new PropertyHolderProperty( propertyHolderType, propertyAccessor, name, type );
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		SimpleConstrainedPropertyHolderElementBuilder that = (SimpleConstrainedPropertyHolderElementBuilder) o;

		if ( !type.equals( that.type ) ) {
			return false;
		}

		return super.equals( o );
	}

	@Override
	public int hashCode() {
		int result = type.hashCode();
		result = 31 * result + super.hashCode();
		return result;
	}
}
