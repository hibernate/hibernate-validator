/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.provider;

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
	 * Returns the annotation processing options as configured by this provider.
	 *
	 * @return The annotation processing options as configured by this provider.
	 */
	AnnotationProcessingOptions getAnnotationProcessingOptions();


	/**
	 * Returns a bean configuration for the given type or {@code null} if this provider has no meta-data on the given
	 * type.
	 */
	<T> BeanConfiguration<? super T> getBeanConfiguration(Class<T> beanClass);
}
