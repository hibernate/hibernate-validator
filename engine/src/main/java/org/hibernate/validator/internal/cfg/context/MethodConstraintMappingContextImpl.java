/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
