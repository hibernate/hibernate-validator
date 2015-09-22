/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

/**
 * Constraint mapping creational context allowing to add cross-parameter constraints to a method or constructor and to
 * navigate to other constraint targets.
 *
 * @author Gunnar Morling
 */
public interface CrossParameterConstraintMappingContext
		extends TypeTarget, ConstructorTarget, MethodTarget, ParameterTarget, ReturnValueTarget, Constrainable<CrossParameterConstraintMappingContext>, AnnotationIgnoreOptions<CrossParameterConstraintMappingContext> {
}
