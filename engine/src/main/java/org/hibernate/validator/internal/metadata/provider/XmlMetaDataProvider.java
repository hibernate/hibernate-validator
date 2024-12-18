/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.metadata.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;
import org.hibernate.validator.internal.xml.mapping.MappingXmlParser;

/**
 * A {@link MetaDataProvider} providing constraint related meta data based on
 * XML descriptors as defined by the Bean Validation API.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class XmlMetaDataProvider implements MetaDataProvider {

	// cached against the fqcn of a class. not a class instance itself (HV-479)
	@Immutable
	private final Map<String, BeanConfiguration<?>> configuredBeans;

	private final AnnotationProcessingOptions annotationProcessingOptions;

	public XmlMetaDataProvider(MappingXmlParser mappingParser) {
		configuredBeans = CollectionHelper.toImmutableMap( createBeanConfigurations( mappingParser ) );
		annotationProcessingOptions = mappingParser.getAnnotationProcessingOptions();
	}

	private static Map<String, BeanConfiguration<?>> createBeanConfigurations(MappingXmlParser mappingParser) {
		final Map<String, BeanConfiguration<?>> configuredBeans = new HashMap<>();
		for ( Class<?> clazz : mappingParser.getXmlConfiguredClasses() ) {
			Set<ConstrainedElement> constrainedElements = mappingParser.getConstrainedElementsForClass( clazz );

			BeanConfiguration<?> beanConfiguration = new BeanConfiguration<>(
					ConfigurationSource.XML,
					clazz,
					constrainedElements,
					mappingParser.getDefaultSequenceForClass( clazz ),
					null
			);

			configuredBeans.put( clazz.getName(), beanConfiguration );
		}
		return configuredBeans;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> BeanConfiguration<T> getBeanConfiguration(Class<T> beanClass) {
		return (BeanConfiguration<T>) configuredBeans.get( beanClass.getName() );
	}

	@Override
	public AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}

	public Set<Class<?>> configuredBeanClasses() {
		return configuredBeans.values().stream().map( BeanConfiguration::getBeanClass )
				.collect( Collectors.toSet() );
	}
}
