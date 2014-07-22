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

import java.util.Set;

import javax.validation.ParameterNameProvider;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Default implementation of {@link ConstraintMapping}.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class DefaultConstraintMapping implements ConstraintMapping {

	private static final Log log = LoggerFactory.make();

	private final AnnotationProcessingOptionsImpl annotationProcessingOptions;
	private final Set<Class<?>> configuredTypes;
	private final Set<TypeConstraintMappingContextImpl<?>> typeContexts;

	public DefaultConstraintMapping() {
		this.annotationProcessingOptions = new AnnotationProcessingOptionsImpl();
		this.configuredTypes = newHashSet();
		this.typeContexts = newHashSet();
	}

	@Override
	public final <C> TypeConstraintMappingContext<C> type(Class<C> type) {
		Contracts.assertNotNull( type, MESSAGES.beanTypeMustNotBeNull() );

		if ( configuredTypes.contains( type ) ) {
			throw log.getBeanClassHasAlreadyBeConfiguredViaProgrammaticApiException( type.getName() );
		}

		TypeConstraintMappingContextImpl<C> typeContext = new TypeConstraintMappingContextImpl<C>( this, type );
		typeContexts.add( typeContext );
		configuredTypes.add( type );

		return typeContext;
	}

	public final AnnotationProcessingOptionsImpl getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}

	public Set<Class<?>> getConfiguredTypes() {
		return configuredTypes;
	}

	/**
	 * Returns all bean configurations configured through this constraint mapping.
	 *
	 * @param constraintHelper constraint helper required for building constraint descriptors
	 * @param parameterNameProvider parameter name provider required for building parameter elements
	 *
	 * @return a set of {@link BeanConfiguration}s with an element for each type configured through this mapping
	 */
	public Set<BeanConfiguration<?>> getBeanConfigurations(ConstraintHelper constraintHelper, ParameterNameProvider parameterNameProvider) {
		Set<BeanConfiguration<?>> configurations = newHashSet();

		for ( TypeConstraintMappingContextImpl<?> typeContext : typeContexts ) {
			configurations.add( typeContext.build( constraintHelper, parameterNameProvider ) );
		}

		return configurations;
	}
}
