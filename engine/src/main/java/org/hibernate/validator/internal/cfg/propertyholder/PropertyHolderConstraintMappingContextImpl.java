/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.propertyholder;

import java.util.Collections;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.propertyholder.PropertyConstraintMappingContext;
import org.hibernate.validator.cfg.propertyholder.PropertyHolderConstraintMappingContext;
import org.hibernate.validator.internal.metadata.core.MetaConstraintBuilder;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.propertyholder.CascadingConstrainedPropertyHolderElementBuilder;
import org.hibernate.validator.internal.metadata.raw.propertyholder.ConstrainedPropertyHolderElementBuilder;

/**
 * Constraint mapping creational context which allows to configure the constraints for one property holder
 * property that is a property holder itself. Hence cascading for it is allowed.
 *
 * @author Marko Bekhta
 */
final class PropertyHolderConstraintMappingContextImpl
		extends CascadablePropertyHolderConstraintMappingContextImplBase<PropertyHolderConstraintMappingContext>
		implements PropertyHolderConstraintMappingContext {

	private final PropertyHolderTypeConstraintMappingContextImpl typeContext;

	PropertyHolderConstraintMappingContextImpl(PropertyHolderTypeConstraintMappingContextImpl typeContext, String property) {
		super( typeContext.getConstraintMapping(), property );
		this.typeContext = typeContext;
	}

	@Override
	protected PropertyHolderConstraintMappingContextImpl getThis() {
		return this;
	}

	@Override
	public PropertyHolderConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {
		super.addConstraint(
				new MetaConstraintBuilder( definition )
		);

		return this;
	}

	@Override
	public PropertyConstraintMappingContext property(String property, Class<?> propertyType) {
		return typeContext.property( property, propertyType );
	}

	@Override
	public PropertyHolderConstraintMappingContext propertyHolder(String property) {
		return typeContext.propertyHolder( property );
	}

	@Override
	protected ConstrainedPropertyHolderElementBuilder build() {
		return new CascadingConstrainedPropertyHolderElementBuilder(
				ConfigurationSource.API,
				property,
				getConstraints(),
				Collections.emptySet(),
				getCascadingMetaDataBuilder()
		);
	}
}
