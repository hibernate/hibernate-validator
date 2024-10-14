/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
public interface ParameterConstraintMappingContext
		extends ConstraintMappingTarget, CrossParameterTarget, ParameterTarget,
		ReturnValueTarget, ConstructorTarget, MethodTarget,
		ContainerElementTarget,
		Constrainable<ParameterConstraintMappingContext>,
		Cascadable<ParameterConstraintMappingContext>,
		AnnotationIgnoreOptions<ParameterConstraintMappingContext> {
}
