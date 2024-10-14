/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.context;

/**
 * Constraint mapping creational context representing a method. Allows to
 * navigate to the method's parameters and return value.
 *
 * @author Gunnar Morling
 */
public interface MethodConstraintMappingContext extends ParameterTarget, CrossParameterTarget, ReturnValueTarget, AnnotationIgnoreOptions<MethodConstraintMappingContext> {
}
