/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

/**
 * Constraint mapping creational context representing a method return value. Allows
 * to place constraints on the return value, mark it as cascadable and to
 * navigate to other constraint targets.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public interface ReturnValueConstraintMappingContext
		extends TypeTarget, ParameterTarget, CrossParameterTarget, ConstructorTarget, MethodTarget, Constrainable<ReturnValueConstraintMappingContext>, Cascadable<ReturnValueConstraintMappingContext>, Unwrapable<ReturnValueConstraintMappingContext>, AnnotationIgnoreOptions<ReturnValueConstraintMappingContext> {

}
