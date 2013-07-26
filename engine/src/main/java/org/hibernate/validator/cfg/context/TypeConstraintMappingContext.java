/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 * @author Gunnar Morling
 */
public interface TypeConstraintMappingContext<C> extends Constrainable<TypeConstraintMappingContext<C>>,
		TypeTarget,
		PropertyTarget,
		MethodTarget,
		ConstructorTarget,
		AnnotationProcessingOptions<TypeConstraintMappingContext<C>> {

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
