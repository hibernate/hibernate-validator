/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.core.MetaConstraints;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.util.TypeResolutionHelper;

/**
 * Base class for implementations of constraint mapping creational context types.
 *
 * @author Gunnar Morling
 */
abstract class ConstraintMappingContextImplBase extends ConstraintContextImplBase {

	private final Set<ConfiguredConstraint<?>> constraints;

	ConstraintMappingContextImplBase(DefaultConstraintMapping mapping) {
		super( mapping );
		this.constraints = newHashSet();
	}

	/**
	 * Returns the type of constraints hosted on the element configured by this creational context.
	 *
	 * @return the type of constraints hosted on the element configured by this creational context
	 */
	protected abstract ConstraintType getConstraintType();

	protected DefaultConstraintMapping getConstraintMapping() {
		return mapping;
	}

	/**
	 * Adds a constraint to the set of constraints managed by this creational context.
	 *
	 * @param constraint the constraint to add
	 */
	protected void addConstraint(ConfiguredConstraint<?> constraint) {
		constraints.add( constraint );
	}

	protected Set<MetaConstraint<?>> getConstraints(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager) {
		if ( constraints == null ) {
			return Collections.emptySet();
		}

		Set<MetaConstraint<?>> metaConstraints = newHashSet();

		for ( ConfiguredConstraint<?> configuredConstraint : constraints ) {
			metaConstraints.add( asMetaConstraint( configuredConstraint, constraintHelper, typeResolutionHelper, valueExtractorManager ) );
		}

		return metaConstraints;
	}

	private <A extends Annotation> MetaConstraint<A> asMetaConstraint(ConfiguredConstraint<A> config, ConstraintHelper constraintHelper,
			TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager) {
		ConstraintDescriptorImpl<A> constraintDescriptor = new ConstraintDescriptorImpl<A>(
				constraintHelper,
				config.getLocation().getMember(),
				config.createAnnotationDescriptor(),
				config.getElementType(),
				getConstraintType()
		);

		return MetaConstraints.create( typeResolutionHelper, valueExtractorManager, constraintDescriptor, config.getLocation() );
	}
}
