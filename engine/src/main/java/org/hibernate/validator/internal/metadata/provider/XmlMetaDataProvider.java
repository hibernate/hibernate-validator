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

import org.hibernate.validator.internal.engine.cascading.ValueExtractors;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.xml.MappingXmlParser;

/**
 * A {@link MetaDataProvider} providing constraint related meta data based on
 * XML descriptors as defined by the Bean Validation API.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class XmlMetaDataProvider extends MetaDataProviderKeyedByClassName {

	private final AnnotationProcessingOptions annotationProcessingOptions;

	public XmlMetaDataProvider(ConstraintHelper constraintHelper,
			TypeResolutionHelper typeResolutionHelper,
			ExecutableParameterNameProvider parameterNameProvider,
			ValueExtractors valueExtractors,
			Set<InputStream> mappingStreams,
			ClassLoader externalClassLoader) {
		this( constraintHelper, typeResolutionHelper, valueExtractors, createMappingParser( constraintHelper, typeResolutionHelper, parameterNameProvider,
				valueExtractors, mappingStreams, externalClassLoader ) );
	}

	private XmlMetaDataProvider(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper, ValueExtractors valueExtractors,
			MappingXmlParser mappingParser) {
		super( constraintHelper, createBeanConfigurations( mappingParser ) );
		annotationProcessingOptions = mappingParser.getAnnotationProcessingOptions();
	}

	private static MappingXmlParser createMappingParser(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ExecutableParameterNameProvider parameterNameProvider, ValueExtractors valueExtractors, Set<InputStream> mappingStreams,
			ClassLoader externalClassLoader) {
		MappingXmlParser mappingParser = new MappingXmlParser( constraintHelper, typeResolutionHelper, parameterNameProvider,
				valueExtractors, externalClassLoader );
		mappingParser.parse( mappingStreams );
		return mappingParser;
	}

	private static Map<String, BeanConfiguration<?>> createBeanConfigurations(MappingXmlParser mappingParser) {
		final Map<String, BeanConfiguration<?>> configuredBeans = new HashMap<>();
		for ( Class<?> clazz : mappingParser.getXmlConfiguredClasses() ) {
			Set<ConstrainedElement> constrainedElements = mappingParser.getConstrainedElementsForClass( clazz );

			BeanConfiguration<?> beanConfiguration = createBeanConfiguration(
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
	public AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}
}
