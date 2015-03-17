/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.provider;

import java.io.InputStream;
import java.util.Set;
import javax.validation.ParameterNameProvider;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.xml.XmlMappingParser;

/**
 * A {@link MetaDataProvider} providing constraint related meta data based on
 * XML descriptors as defined by the Bean Validation API.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class XmlMetaDataProvider extends MetaDataProviderKeyedByClassName {

	private final AnnotationProcessingOptions annotationProcessingOptions;

	/**
	 * @param constraintHelper the constraint helper
	 * @param parameterNameProvider the name provider
	 * @param mappingStreams the input stream for the xml configuration
	 * @param externalClassLoader user provided class loader for the loading of XML mapping files
	 */
	public XmlMetaDataProvider(ConstraintHelper constraintHelper,
							   ParameterNameProvider parameterNameProvider,
							   Set<InputStream> mappingStreams,
							   ClassLoader externalClassLoader) {
		super( constraintHelper );

		XmlMappingParser mappingParser = new XmlMappingParser( constraintHelper, parameterNameProvider, externalClassLoader );
		mappingParser.parse( mappingStreams );

		for ( Class<?> clazz : mappingParser.getXmlConfiguredClasses() ) {
			Set<ConstrainedElement> constrainedElements = mappingParser.getConstrainedElementsForClass( clazz );

			BeanConfiguration<?> beanConfiguration = createBeanConfiguration(
					ConfigurationSource.XML,
					clazz,
					constrainedElements,
					mappingParser.getDefaultSequenceForClass( clazz ),
					null
			);
			addBeanConfiguration(
					clazz,
					beanConfiguration
			);
		}

		annotationProcessingOptions = mappingParser.getAnnotationProcessingOptions();
	}

	@Override
	public AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}
}
