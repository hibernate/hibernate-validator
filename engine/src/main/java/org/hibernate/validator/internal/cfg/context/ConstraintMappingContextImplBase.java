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

	// TODO Snapshot the constraint definitions here, i.e. call ConfiguredConstraint#createAnnotationDescriptor()
	// while materializing instead of leaving it to the metadata providers. As it stands, only the list of definitions
	// is snapshotted, not their contents: mutating a ConstraintDef after a factory has been built still changes that
	// factory, because the descriptor is created lazily, when ProgrammaticMetaDataProvider builds the bean
	// configurations. MappingMaterializationTest#laterDeclarationsDoNotChangeAnExistingFactory only covers adding
	// definitions, so it does not catch this. Pre-existing behavior, kept as is so that this refactoring does not
	// change semantics.
	protected Set<ConfiguredConstraint<?>> materializeConstraints(ConstraintLocation location) {
		Set<ConfiguredConstraint<?>> resolved = newHashSet();
		for ( ConstraintDef<?, ?> definition : constraints ) {
			resolved.add( ConfiguredConstraint.forLocation( definition, location ) );
		}
		return resolved;
	}
}
