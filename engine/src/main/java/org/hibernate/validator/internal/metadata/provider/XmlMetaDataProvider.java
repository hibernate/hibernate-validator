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
	 * @param mappingStreams the input stream for the xml configuration
	 */
	public XmlMetaDataProvider(ConstraintHelper constraintHelper,
							   ParameterNameProvider parameterNameProvider,
							   Set<InputStream> mappingStreams) {
		super( constraintHelper );

		XmlMappingParser mappingParser = new XmlMappingParser( constraintHelper, parameterNameProvider );
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
