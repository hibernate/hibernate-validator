/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.provider;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
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

	public XmlMetaDataProvider(ConstraintCreationContext constraintCreationContext,
			JavaBeanHelper javaBeanHelper,
			Set<InputStream> mappingStreams,
			ClassLoader externalClassLoader) {

		MappingXmlParser mappingParser = new MappingXmlParser( constraintCreationContext,
				javaBeanHelper, externalClassLoader );
		mappingParser.parse( mappingStreams );

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
}
