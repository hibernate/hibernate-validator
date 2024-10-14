/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.context;

/**
 * Constraint mapping creational context representing a constructor. Allows to
 * navigate to the constructor's parameters and return value.
 *
 * @author Gunnar Morling
 */
public interface ConstructorConstraintMappingContext extends ParameterTarget, CrossParameterTarget, ReturnValueTarget, AnnotationIgnoreOptions<ConstructorConstraintMappingContext> {

}
