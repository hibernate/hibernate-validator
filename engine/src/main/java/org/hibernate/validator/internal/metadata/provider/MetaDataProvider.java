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
package org.hibernate.validator.internal.metadata.provider;

import java.util.List;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;

/**
 * A provider for constraint related meta data such as constraints, default
 * group sequences etc.
 * <p>
 * Implementations are based one different meta data sources such as XML,
 * programmatic mappings and annotations. Meta data providers only return meta
 * data directly configured for one class, they don't deal with merging meta
 * data from super-classes or implemented interfaces.
 * </p>
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public interface MetaDataProvider {
	/**
	 * Used as prefix for parameter names, if no explicit names are given.
	 */
	static final String DEFAULT_PARAMETER_NAME_PREFIX = "arg";

	/**
	 * Returns the annotation processing options as configured by this provider.
	 *
	 * @return The annotation processing options as configured by this provider.
	 */
	AnnotationProcessingOptions getAnnotationProcessingOptions();

	/**
	 * Returns a list with the configurations for all types contained in the
	 * given type's hierarchy (including implemented interfaces) starting at the
	 * specified type.
	 *
	 * @param beanClass The type of interest.
	 *
	 * @return A set with the configurations for the complete hierarchy of the
	 *         given type. May be empty, but never {@code null}.
	 */
	<T> List<BeanConfiguration<? super T>> getBeanConfigurationForHierarchy(Class<T> beanClass);
}
