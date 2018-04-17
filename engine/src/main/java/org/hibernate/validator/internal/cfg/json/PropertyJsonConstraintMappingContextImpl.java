/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.json;

import java.util.Collections;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.json.PropertyConstraintMappingContext;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedProperty;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.util.TypeResolutionHelper;

/**
 * Constraint mapping creational context which allows to configure the constraints for one bean property.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
final class PropertyJsonConstraintMappingContextImpl
		extends CascadableJsonConstraintMappingContextImplBase<PropertyConstraintMappingContext>
		implements PropertyConstraintMappingContext {

	private final JsonTypeConstraintMappingContextImpl<?> typeContext;

	private final Property property;

	PropertyJsonConstraintMappingContextImpl(JsonTypeConstraintMappingContextImpl<?> typeContext, Property property) {
		super( typeContext.getConstraintMapping(), property.getType() );
		this.typeContext = typeContext;
		this.property = property;
	}

	@Override
	protected PropertyJsonConstraintMappingContextImpl getThis() {
		return this;
	}

	@Override
	public PropertyConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {
		super.addConstraint(
				ConfiguredConstraint.forProperty(
						definition, property
				)
		);
		return this;
	}

	@Override
	public PropertyConstraintMappingContext property(String property, Class<?> propertyType) {
		return typeContext.property( property, propertyType );
	}

	@Override
	public PropertyConstraintMappingContext property(String property) {
		return property( property, String.class );
	}

	ConstrainedElement build(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager) {
		return ConstrainedProperty.forField(
				ConfigurationSource.API,
				property,
				getConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager ),
				Collections.emptySet(),
				getCascadingMetaDataBuilder()
		);
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.GENERIC;
	}
}
