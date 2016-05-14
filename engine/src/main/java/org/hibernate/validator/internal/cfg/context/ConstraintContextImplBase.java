/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
