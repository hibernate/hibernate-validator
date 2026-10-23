/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.reflect.Constructor;

import org.hibernate.validator.cfg.context.ConstructorConstraintMappingContext;

/**
 * Constraint mapping creational context representing a constructor.
 *
 * @author Gunnar Morling
 */
class ConstructorConstraintMappingContextImpl extends ExecutableConstraintMappingContextImpl implements ConstructorConstraintMappingContext {

	<T> ConstructorConstraintMappingContextImpl(TypeConstraintMappingContextImpl<T> typeContext, Constructor<?> constructor) {
		super( typeContext, constructor );
	}

	@Override
	public ConstructorConstraintMappingContext ignoreAnnotations(boolean ignoreAnnotations) {
		this.ignoreAnnotations = ignoreAnnotations;

		return this;
	}
}
