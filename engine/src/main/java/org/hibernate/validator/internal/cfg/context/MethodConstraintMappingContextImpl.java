/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.cfg.context;

import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.internal.properties.javabean.JavaBeanMethod;

/**
 * Constraint mapping creational context representing a method.
 *
 * @author Gunnar Morling
 */
class MethodConstraintMappingContextImpl extends ExecutableConstraintMappingContextImpl implements MethodConstraintMappingContext {

	MethodConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, JavaBeanMethod callable) {
		super( typeContext, callable );
	}

	@Override
	public MethodConstraintMappingContext ignoreAnnotations(boolean ignoreAnnotations) {
		typeContext.mapping
				.getAnnotationProcessingOptions()
				.ignoreConstraintAnnotationsOnMember( callable, ignoreAnnotations );

		return this;
	}
}
