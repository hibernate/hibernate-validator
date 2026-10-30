/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.cfg.context;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;

/**
 * Base class for implementations of constraint mapping creational context types.
 *
 * @author Gunnar Morling
 */
abstract class ConstraintMappingContextImplBase extends ConstraintContextImplBase {

	private final List<ConstraintDef<?, ?>> constraints = new ArrayList<>();
	protected Boolean ignoreAnnotations;

	ConstraintMappingContextImplBase(DefaultConstraintMapping mapping) {
		super( mapping );
	}

	protected DefaultConstraintMapping getConstraintMapping() {
		return mapping;
	}

	protected void addConstraint(ConstraintDef<?, ?> constraint) {
		constraints.add( constraint );
	}

	protected Set<ConfiguredConstraint<?>> materializeConstraints(ConstraintLocation location) {
		Set<ConfiguredConstraint<?>> resolved = newHashSet();
		for ( ConstraintDef<?, ?> definition : constraints ) {
			resolved.add( ConfiguredConstraint.forLocation( definition, location ) );
		}
		return resolved;
	}
}
