/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
	 * @param <T> The type of the class to get the configurations for.
	 *
	 * @return A set with the configurations for the complete hierarchy of the
	 *         given type. May be empty, but never {@code null}.
	 */
	<T> List<BeanConfiguration<? super T>> getBeanConfigurationForHierarchy(Class<T> beanClass);
}
