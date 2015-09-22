/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.provider;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * Base implementation for {@link MetaDataProvider}s which cache the {@code BeanConfiguration} by class name.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public abstract class MetaDataProviderKeyedByClassName implements MetaDataProvider {
	protected final ConstraintHelper constraintHelper;
	// cached against the fqcn of a class. not a class instance itself (HV-479)
	private final Map<String, BeanConfiguration<?>> configuredBeans;

	public MetaDataProviderKeyedByClassName(ConstraintHelper constraintHelper, Map<String, BeanConfiguration<?>> configuredBeans) {
		this.constraintHelper = constraintHelper;
		this.configuredBeans = Collections.unmodifiableMap( configuredBeans );
	}

	@Override
	public <T> List<BeanConfiguration<? super T>> getBeanConfigurationForHierarchy(Class<T> beanClass) {
		List<BeanConfiguration<? super T>> configurations = newArrayList();

		for ( Class<? super T> clazz : ClassHierarchyHelper.getHierarchy( beanClass ) ) {
			BeanConfiguration<? super T> configuration = getBeanConfiguration( clazz );
			if ( configuration != null ) {
				configurations.add( configuration );
			}
		}

		return configurations;
	}

	@SuppressWarnings("unchecked")
	protected <T> BeanConfiguration<T> getBeanConfiguration(Class<T> beanClass) {
		Contracts.assertNotNull( beanClass );
		return (BeanConfiguration<T>) configuredBeans.get( beanClass.getName() );
	}

	protected static <T> BeanConfiguration<T> createBeanConfiguration(ConfigurationSource source,
															   Class<T> beanClass,
															   Set<? extends ConstrainedElement> constrainedElements,
															   List<Class<?>> defaultGroupSequence,
															   DefaultGroupSequenceProvider<? super T> defaultGroupSequenceProvider) {
		return new BeanConfiguration<T>(
				source,
				beanClass,
				constrainedElements,
				defaultGroupSequence,
				defaultGroupSequenceProvider
		);
	}
}
