/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import org.hibernate.validator.cfg.context.ConstructorConstraintMappingContext;
import org.hibernate.validator.internal.properties.javabean.JavaBeanConstructor;

/**
 * Constraint mapping creational context representing a constructor.
 *
 * @author Gunnar Morling
 */
class ConstructorConstraintMappingContextImpl extends ExecutableConstraintMappingContextImpl implements ConstructorConstraintMappingContext {

	<T> ConstructorConstraintMappingContextImpl(TypeConstraintMappingContextImpl<T> typeContext, JavaBeanConstructor constructor) {
		super( typeContext, constructor );
	}

	@Override
	public ConstructorConstraintMappingContext ignoreAnnotations(boolean ignoreAnnotations) {
		typeContext.mapping
				.getAnnotationProcessingOptions()
				.ignoreConstraintAnnotationsOnMember( callable, ignoreAnnotations );

		return this;
	}
}
