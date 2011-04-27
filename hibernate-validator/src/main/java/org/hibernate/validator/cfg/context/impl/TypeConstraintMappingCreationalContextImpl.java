/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.cfg.context.impl;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import org.hibernate.validator.cfg.ConfiguredConstraint;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.context.TypeConstraintMappingCreationalContext;
import org.hibernate.validator.group.DefaultGroupSequenceProvider;

/**
 * Constraint mapping creational context which allows to configure the class-level constraints for one bean.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class TypeConstraintMappingCreationalContextImpl extends ConstraintMappingCreationalContextImplBase
		implements TypeConstraintMappingCreationalContext {

	public TypeConstraintMappingCreationalContextImpl(Class<?> beanClass, ConstraintMapping mapping) {
		super( beanClass, mapping );
	}

	public TypeConstraintMappingCreationalContext constraint(ConstraintDef<?, ?> definition) {

		mapping.addConstraintConfig( getConfiguredConstraint( definition ) );
		return this;
	}

	/**
	 * Defines the default groups sequence for the bean class of this instance.
	 *
	 * @param defaultGroupSequence the default group sequence.
	 *
	 * @return Returns itself for method chaining.
	 */
	public TypeConstraintMappingCreationalContext defaultGroupSequence(Class<?>... defaultGroupSequence) {
		mapping.addDefaultGroupSequence( beanClass, Arrays.asList( defaultGroupSequence ) );
		return this;
	}

	/**
	 * Defines the default group sequence provider for the bean class of this instance.
	 *
	 * @param defaultGroupSequenceProviderClass The default group sequence provider class.
	 *
	 * @return Returns itself for method chaining.
	 */
	public <T extends DefaultGroupSequenceProvider<?>> TypeConstraintMappingCreationalContext defaultGroupSequenceProvider(Class<T> defaultGroupSequenceProviderClass) {
		mapping.addDefaultGroupSequenceProvider( beanClass, defaultGroupSequenceProviderClass );
		return this;
	}

	private <A extends Annotation> ConfiguredConstraint<A> getConfiguredConstraint(ConstraintDef<?, A> definition) {
		return ConfiguredConstraint.forType(
				definition, beanClass
		);
	}
}
