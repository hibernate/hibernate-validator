/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.context;

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

/**
 * Constraint mapping creational context representing a type. Allows place
 * class-level constraints on that type, define its default group sequence (and provider)
 * and to navigate to other constraint targets.
 *
 * @param <C> The type represented by this creational context.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Gunnar Morling
 */
public interface TypeConstraintMappingContext<C>
		extends Constrainable<TypeConstraintMappingContext<C>>,
		ConstraintMappingTarget,
		PropertyTarget,
		MethodTarget,
		ConstructorTarget,
		AnnotationIgnoreOptions<TypeConstraintMappingContext<C>> {

	/**
	 * Defines that all annotations for this type should be ignored.
	 *
	 * @return The current creational context following the method chaining pattern.
	 */
	TypeConstraintMappingContext<C> ignoreAllAnnotations();

	/**
	 * Defines the default group sequence for current type.
	 *
	 * @param defaultGroupSequence the default group sequence.
	 *
	 * @return The current creational context following the method chaining pattern.
	 */
	TypeConstraintMappingContext<C> defaultGroupSequence(Class<?>... defaultGroupSequence);

	/**
	 * Defines the default group sequence provider for the current type.
	 *
	 * @param defaultGroupSequenceProviderClass The default group sequence provider class.
	 *
	 * @return The current creational context following the method chaining pattern.
	 */
	TypeConstraintMappingContext<C> defaultGroupSequenceProviderClass(
			Class<? extends DefaultGroupSequenceProvider<? super C>> defaultGroupSequenceProviderClass);
}
