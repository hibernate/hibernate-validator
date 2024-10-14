/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
		extends ConstraintMappingTarget, ParameterTarget, CrossParameterTarget, ConstructorTarget, MethodTarget,
		ContainerElementTarget, Constrainable<ReturnValueConstraintMappingContext>, Cascadable<ReturnValueConstraintMappingContext>,
		AnnotationIgnoreOptions<ReturnValueConstraintMappingContext> {

}
