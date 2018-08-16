/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.propertyholder;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.util.Set;

import org.hibernate.validator.cfg.propertyholder.TypeConstraintMappingContext;
import org.hibernate.validator.internal.metadata.core.MetaConstraintBuilder;

/**
 * Base class for implementations of constraint-related context types.
 *
 * @author Marko Bekhta
 */
abstract class ConstraintContextImplBase {

	protected final PropertyHolderConstraintMappingImpl mapping;

	private final Set<MetaConstraintBuilder<?>> constraints;

	public ConstraintContextImplBase(PropertyHolderConstraintMappingImpl mapping) {
		this.mapping = mapping;
		this.constraints = newHashSet();
	}

	public TypeConstraintMappingContext type(String propertyHolderMappingName) {
		return mapping.type( propertyHolderMappingName );
	}

	protected PropertyHolderConstraintMappingImpl getConstraintMapping() {
		return mapping;
	}

	protected void addConstraint(MetaConstraintBuilder<?> constraint) {
		constraints.add( constraint );
	}

	protected Set<MetaConstraintBuilder<?>> getConstraints() {
		return constraints;
	}
}
