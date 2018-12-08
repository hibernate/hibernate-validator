/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.properties.javabean.JavaBeanField;

/**
 * An implementation of {@link AbstractPropertyConstraintMappingContextImpl} for a field property.
 * Represents a constraint mapping creational context which allows to configure the constraints
 * for one of the bean's field properties.
 *
 * @author Marko Bekhta
 */
final class FieldConstraintMappingContextImpl extends AbstractPropertyConstraintMappingContextImpl<JavaBeanField> {

	FieldConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, JavaBeanField javaBeanField) {
		super( typeContext, javaBeanField, ConstraintLocation.forField( javaBeanField ) );
	}

	@Override
	public PropertyConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {
		super.addConstraint(
				ConfiguredConstraint.forField(
						definition, getProperty()
				)
		);
		return this;
	}

	@Override
	ConstrainedElement build(ConstraintCreationContext constraintCreationContext) {
		return new ConstrainedField(
				ConfigurationSource.API,
				getProperty(),
				getConstraints( constraintCreationContext ),
				getTypeArgumentConstraints( constraintCreationContext ),
				getCascadingMetaDataBuilder()
		);
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.GENERIC;
	}
}
