/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.reflect.Method;

import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;

/**
 * Constraint mapping creational context representing a method.
 *
 * @author Gunnar Morling
 */
class MethodConstraintMappingContextImpl extends ExecutableConstraintMappingContextImpl implements MethodConstraintMappingContext {

	MethodConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, Method callable) {
		super( typeContext, callable );
	}

	@Override
	public MethodConstraintMappingContext ignoreAnnotations(boolean ignoreAnnotations) {
		this.ignoreAnnotations = ignoreAnnotations;

		return this;
	}
}
