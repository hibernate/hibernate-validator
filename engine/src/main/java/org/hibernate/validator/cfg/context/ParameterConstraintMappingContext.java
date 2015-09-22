/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

/**
 * Constraint mapping creational context representing a method parameter. Allows
 * to place constraints on the parameter, mark the parameter as cascadable and to
 * navigate to other constraint targets.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public interface ParameterConstraintMappingContext extends TypeTarget, CrossParameterTarget, ParameterTarget,
		ReturnValueTarget, ConstructorTarget, MethodTarget, Constrainable<ParameterConstraintMappingContext>,
		Cascadable<ParameterConstraintMappingContext>, Unwrapable<ParameterConstraintMappingContext>,
		AnnotationIgnoreOptions<ParameterConstraintMappingContext> {
}
