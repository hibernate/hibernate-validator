/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.cfg.context;

import org.hibernate.validator.cfg.context.GroupConversionTargetContext;

/**
 * Context allowing to set the target of a group conversion.
 *
 * @author Gunnar Morling
 */
class GroupConversionTargetContextImpl<C> implements GroupConversionTargetContext<C> {

	private final C cascadableContext;
	private final Class<?> from;
	private final CascadableConstraintMappingContextImplBase<?> target;

	GroupConversionTargetContextImpl(Class<?> from, C cascadableContext, CascadableConstraintMappingContextImplBase<?> target) {
		this.from = from;
		this.cascadableContext = cascadableContext;
		this.target = target;
	}

	@Override
	public C to(Class<?> to) {
		target.addGroupConversion( from, to );
		return cascadableContext;
	}
}
