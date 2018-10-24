/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw.propertyholder;

import java.util.Set;

import org.hibernate.validator.internal.metadata.aggregated.cascading.PropertyHolderCascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.MetaConstraintBuilder;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.properties.PropertyAccessor;
import org.hibernate.validator.internal.properties.propertyholder.PropertyAccessorCreatorProvider;
import org.hibernate.validator.internal.properties.propertyholder.PropertyHolderProperty;
import org.hibernate.validator.spi.propertyholder.PropertyAccessorCreator;

/**
 * @author Marko Bekhta
 */
public class CascadingConstrainedPropertyHolderElementBuilder extends ConstrainedPropertyHolderElementBuilder {

	public CascadingConstrainedPropertyHolderElementBuilder(ConfigurationSource source,
			String name, Set<MetaConstraintBuilder<?>> constraints,
			Set<MetaConstraintBuilder<?>> typeArgumentConstraints,
			PropertyHolderCascadingMetaDataBuilder cascadingMetaDataBuilder) {
		super( source, name, constraints, typeArgumentConstraints, cascadingMetaDataBuilder );
	}

	@Override
	protected PropertyHolderProperty createPropertyHolderProperty(PropertyAccessorCreatorProvider propertyAccessorCreatorProvider, Class<?> propertyHolderType) {
		PropertyAccessorCreator<?> propertyAccessorCreator = propertyAccessorCreatorProvider.getPropertyAccessorCreatorFor( propertyHolderType );
		PropertyAccessor propertyAccessor = propertyAccessorCreator.create( name, propertyHolderType );

		return new PropertyHolderProperty( propertyHolderType, propertyAccessor, name, propertyHolderType );
	}
}
