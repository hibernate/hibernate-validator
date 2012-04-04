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
package org.hibernate.validator.internal.cfg.context;

import java.util.Arrays;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;
import org.hibernate.validator.group.DefaultGroupSequenceProvider;

/**
 * Constraint mapping creational context which allows to configure the class-level constraints for one bean.
 *
 * @param <C> The type represented by this creational context.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
@SuppressWarnings("deprecation")
public final class TypeConstraintMappingContextImpl<C> extends ConstraintMappingContextImplBase
		implements TypeConstraintMappingContext<C> {

	public TypeConstraintMappingContextImpl(Class<?> beanClass, ConstraintMappingContext mapping) {
		super( beanClass, mapping );
		mapping.getAnnotationProcessingOptions().ignoreAnnotationConstraintForClass( beanClass, Boolean.FALSE );
	}

	public TypeConstraintMappingContext<C> constraint(ConstraintDef<?, ?> definition) {
		mapping.addConstraintConfig( ConfiguredConstraint.forType( definition, beanClass ) );
		return this;
	}

	public TypeConstraintMappingContext<C> ignoreAnnotations() {
		mapping.getAnnotationProcessingOptions().ignoreClassLevelConstraintAnnotations( beanClass, Boolean.TRUE );
		return this;
	}

	public TypeConstraintMappingContext<C> ignoreAllAnnotations() {
		mapping.getAnnotationProcessingOptions().ignoreAnnotationConstraintForClass( beanClass, Boolean.TRUE );
		return this;
	}

	public TypeConstraintMappingContext<C> defaultGroupSequence(Class<?>... defaultGroupSequence) {
		mapping.addDefaultGroupSequence( beanClass, Arrays.asList( defaultGroupSequence ) );
		return this;
	}

	public <T extends DefaultGroupSequenceProvider<? super C>> TypeConstraintMappingContext<C> defaultGroupSequenceProvider(Class<T> defaultGroupSequenceProviderClass) {
		@SuppressWarnings("unchecked")
		Class<C> clazz = (Class<C>) beanClass;
		mapping.addDeprecatedDefaultGroupSequenceProvider( clazz, defaultGroupSequenceProviderClass );
		return this;
	}

	public TypeConstraintMappingContext<C> defaultGroupSequenceProviderClass(Class<? extends org.hibernate.validator.spi.group.DefaultGroupSequenceProvider<? super C>> defaultGroupSequenceProviderClass) {
		@SuppressWarnings("unchecked")
		Class<C> clazz = (Class<C>) beanClass;
		mapping.addDefaultGroupSequenceProvider( clazz, defaultGroupSequenceProviderClass );
		return this;
	}
}
