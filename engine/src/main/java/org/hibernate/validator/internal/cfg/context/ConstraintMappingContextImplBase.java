/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Base class for implementations of constraint mapping creational context types.
 *
 * @author Gunnar Morling
 */
abstract class ConstraintMappingContextImplBase {

	protected final DefaultConstraintMapping mapping;
	private final Set<ConfiguredConstraint<?>> constraints;

	ConstraintMappingContextImplBase(DefaultConstraintMapping mapping) {
		this.mapping = mapping;
		this.constraints = newHashSet();
	}

	public <C> TypeConstraintMappingContext<C> type(Class<C> type) {
		return mapping.type( type );
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

	/**
	 * Converts all constraints managed by this creational context into {@link MetaConstraint}s.
	 *
	 * @param constraintHelper constraint helper required for creation of meta constraints.
	 *
	 * @return All constraints of this context as meta constraints.
	 */
	protected Set<MetaConstraint<?>> getConstraints(ConstraintHelper constraintHelper) {
		if ( constraints == null ) {
			return Collections.emptySet();
		}

		Set<MetaConstraint<?>> metaConstraints = newHashSet();

		for ( ConfiguredConstraint<?> configuredConstraint : constraints ) {
			metaConstraints.add( asMetaConstraint( configuredConstraint, constraintHelper ) );
		}

		return metaConstraints;
	}

	private <A extends Annotation> MetaConstraint<A> asMetaConstraint(ConfiguredConstraint<A> config, ConstraintHelper constraintHelper) {
		ConstraintDescriptorImpl<A> constraintDescriptor = new ConstraintDescriptorImpl<A>(
				constraintHelper,
				config.getLocation().getMember(),
				config.createAnnotationProxy(),
				config.getElementType(),
				getConstraintType()
		);

		return new MetaConstraint<A>( constraintDescriptor, config.getLocation() );
	}
}
