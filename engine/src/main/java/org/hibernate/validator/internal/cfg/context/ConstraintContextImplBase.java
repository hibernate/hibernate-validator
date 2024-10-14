/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.annotation.Annotation;

import org.hibernate.validator.cfg.context.ConstraintDefinitionContext;
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;

/**
 * Base class for implementations of constraint-related context types.
 *
 * @author Gunnar Morling
 * @author Yoann Rodiere
 */
abstract class ConstraintContextImplBase {

	protected final DefaultConstraintMapping mapping;

	public ConstraintContextImplBase(DefaultConstraintMapping mapping) {
		this.mapping = mapping;
	}

	public <C> TypeConstraintMappingContext<C> type(Class<C> type) {
		return mapping.type( type );
	}

	public <A extends Annotation> ConstraintDefinitionContext<A> constraintDefinition(Class<A> annotationClass) {
		return mapping.constraintDefinition( annotationClass );
	}

}
