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
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.properties.javabean.JavaBeanGetter;

/**
 * An implementation of {@link AbstractPropertyConstraintMappingContextImpl} for a getter property.
 * Represents a constraint mapping creational context which allows to configure the constraints
 * for one of the bean's getter properties.
 *
 * @author Marko Bekhta
 */
final class GetterConstraintMappingContextImpl extends AbstractPropertyConstraintMappingContextImpl<JavaBeanGetter> {

	GetterConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, JavaBeanGetter javaBeanGetter) {
		super( typeContext, javaBeanGetter, ConstraintLocation.forGetter( javaBeanGetter ) );
	}

	@Override
	public PropertyConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {
		super.addConstraint(
				ConfiguredConstraint.forGetter(
						definition, getProperty()
				)
		);
		return this;
	}

	@Override
	ConstrainedElement build(ConstraintCreationContext constraintCreationContext) {
		return new ConstrainedExecutable(
				ConfigurationSource.API,
				getProperty(),
				getConstraints( constraintCreationContext ),
				getTypeArgumentConstraints( constraintCreationContext ),
				getCascadingMetaDataBuilder()
		);
	}
}
