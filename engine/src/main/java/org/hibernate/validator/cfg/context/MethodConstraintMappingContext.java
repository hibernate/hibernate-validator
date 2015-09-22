/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
