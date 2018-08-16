/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.propertyholder;

import org.hibernate.validator.internal.metadata.raw.propertyholder.ConstrainedPropertyHolderElementBuilder;

/**
 * Base class for implementations of constraint mapping creational context types.
 *
 * @author Marko Bekhta
 */
abstract class PropertyConstraintMappingContextImplBase extends ConstraintContextImplBase {

	protected final String property;

	PropertyConstraintMappingContextImplBase(PropertyHolderConstraintMappingImpl mapping, String property) {
		super( mapping );
		this.property = property;
	}

	protected abstract ConstrainedPropertyHolderElementBuilder build();
}
