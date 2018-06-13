/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
@SuppressWarnings("deprecation")
public interface PropertyConstraintMappingContext extends Constrainable<PropertyConstraintMappingContext>,
		ConstraintMappingTarget,
		PropertyTarget,
		ConstructorTarget,
		MethodTarget,
		ContainerElementTarget,
		Cascadable<PropertyConstraintMappingContext>,
		AnnotationProcessingOptions<PropertyConstraintMappingContext>,
		AnnotationIgnoreOptions<PropertyConstraintMappingContext> {
}
