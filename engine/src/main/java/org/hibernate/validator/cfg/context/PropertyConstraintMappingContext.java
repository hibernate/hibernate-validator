/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.context;

/**
 * Constraint mapping creational context representing a property of a bean. Allows
 * to place constraints on the property, mark the property as cascadable and to
 * navigate to other constraint targets.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public interface PropertyConstraintMappingContext
		extends Constrainable<PropertyConstraintMappingContext>,
		ConstraintMappingTarget,
		PropertyTarget,
		ConstructorTarget,
		MethodTarget,
		ContainerElementTarget,
		Cascadable<PropertyConstraintMappingContext>,
		AnnotationIgnoreOptions<PropertyConstraintMappingContext> {
}
