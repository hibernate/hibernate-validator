/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

/**
 * Constraint mapping creational context representing a type argument of a property, parameter or method return value
 * with a generic (return) type. Allows to place constraints on that type argument, mark it as cascadable and to
 * navigate to other constraint targets.
 *
 * @author Gunnar Morling
 *
 * @since 6.0
 */
public interface ContainerElementConstraintMappingContext extends Constrainable<ContainerElementConstraintMappingContext>,
	ConstraintMappingTarget,
	PropertyTarget,
	ConstructorTarget,
	MethodTarget,
	ContainerElementTarget,
	ParameterTarget,
	ReturnValueTarget,
	Cascadable<ContainerElementConstraintMappingContext> {
}
