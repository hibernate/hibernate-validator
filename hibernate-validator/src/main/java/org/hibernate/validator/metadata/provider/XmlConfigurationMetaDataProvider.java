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
package org.hibernate.validator.metadata.provider;

import java.io.InputStream;
import java.lang.reflect.Member;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.metadata.AnnotationIgnores;
import org.hibernate.validator.metadata.BeanConfiguration;
import org.hibernate.validator.metadata.BeanMetaConstraint;
import org.hibernate.validator.metadata.ConstraintHelper;
import org.hibernate.validator.xml.XmlMappingParser;

/**
 * @author Gunnar Morling
 */
public class XmlConfigurationMetaDataProvider extends MetaDataProviderImplBase {

	private final AnnotationIgnores annotationIgnores;

	/**
	 * @param mappingStreams
	 */
	public XmlConfigurationMetaDataProvider(ConstraintHelper constraintHelper, Set<InputStream> mappingStreams) {

		super( constraintHelper );

		XmlMappingParser mappingParser = new XmlMappingParser( constraintHelper );
		mappingParser.parse( mappingStreams );

		for ( Class<?> clazz : mappingParser.getXmlConfiguredClasses() ) {

			configuredBeans.put(
					clazz,
					createBeanConfiguration(
							clazz,
							new HashSet<BeanMetaConstraint<?>>( mappingParser.getConstraintsForClass( clazz ) ),
							new HashSet<Member>( mappingParser.getCascadedMembersForClass( clazz ) ),
							mappingParser.getDefaultSequenceForClass( clazz ),
							null
					)
			);
		}

		annotationIgnores = mappingParser.getAnnotationIgnores();
	}

	public Set<BeanConfiguration<?>> getAllBeanConfigurations() {
		return new HashSet<BeanConfiguration<?>>( configuredBeans.values() );
	}

	public AnnotationIgnores getAnnotationIgnores() {
		return annotationIgnores;
	}

}
