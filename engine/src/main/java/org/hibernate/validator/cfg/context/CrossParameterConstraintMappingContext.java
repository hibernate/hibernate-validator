/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.context;

/**
 * Constraint mapping creational context allowing to add cross-parameter constraints to a method or constructor and to
 * navigate to other constraint targets.
 *
 * @author Gunnar Morling
 */
public interface CrossParameterConstraintMappingContext
		extends ConstraintMappingTarget, ConstructorTarget, MethodTarget, ParameterTarget, ReturnValueTarget, Constrainable<CrossParameterConstraintMappingContext>,
		AnnotationIgnoreOptions<CrossParameterConstraintMappingContext> {
}
