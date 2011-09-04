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
import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.metadata.AnnotationIgnores;
import org.hibernate.validator.metadata.ConstraintHelper;
import org.hibernate.validator.metadata.MetaConstraint;
import org.hibernate.validator.metadata.constrained.ConstrainedElement;
import org.hibernate.validator.metadata.constrained.ConstrainedElement.ConfigurationSource;
import org.hibernate.validator.metadata.constrained.ConstrainedField;
import org.hibernate.validator.metadata.constrained.ConstrainedMethod;
import org.hibernate.validator.metadata.constrained.ConstrainedType;
import org.hibernate.validator.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.metadata.location.ConstraintLocation;
import org.hibernate.validator.metadata.location.MethodConstraintLocation;
import org.hibernate.validator.util.CollectionHelper.Partitioner;
import org.hibernate.validator.xml.XmlMappingParser;

import static org.hibernate.validator.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.util.CollectionHelper.partition;

/**
 * A {@link MetaDataProvider} providing constraint related meta data based on
 * XML descriptors as defined by the Bean Validation API.
 *
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

			Map<ConstraintLocation, Set<MetaConstraint<?>>> constraintsByLocation = partition(
					mappingParser.getConstraintsForClass( clazz ), byLocation()
			);
			Set<BeanConstraintLocation> cascades = getCascades( mappingParser, clazz );

			Set<ConstrainedElement> constrainedElements = getConstrainedElements( constraintsByLocation, cascades );

			configuredBeans.put(
					clazz,
					createBeanConfiguration(
							clazz,
							constrainedElements,
							mappingParser.getDefaultSequenceForClass( clazz ),
							null
					)
			);
		}

		annotationIgnores = mappingParser.getAnnotationIgnores();
	}

	private Set<ConstrainedElement> getConstrainedElements(Map<ConstraintLocation, Set<MetaConstraint<?>>> constraintsByLocation, Set<BeanConstraintLocation> cascades) {

		Set<ConstraintLocation> configuredLocations = new HashSet<ConstraintLocation>( cascades );
		configuredLocations.addAll( constraintsByLocation.keySet() );

		Set<ConstrainedElement> propertyMetaData = newHashSet();

		for ( ConstraintLocation oneConfiguredLocation : configuredLocations ) {
			if ( oneConfiguredLocation.getElementType() == ElementType.FIELD ) {
				propertyMetaData.add(
						new ConstrainedField(
								ConfigurationSource.XML,
								(BeanConstraintLocation) oneConfiguredLocation,
								constraintsByLocation.get( oneConfiguredLocation ),
								cascades.contains( oneConfiguredLocation )
						)
				);
			}
			else if ( oneConfiguredLocation.getElementType() == ElementType.METHOD ) {
				propertyMetaData.add(
						new ConstrainedMethod(
								ConfigurationSource.XML,
								new MethodConstraintLocation( (java.lang.reflect.Method) oneConfiguredLocation.getMember() ),
								constraintsByLocation.get( oneConfiguredLocation ),
								cascades.contains( oneConfiguredLocation )
						)
				);
			}
			else if ( oneConfiguredLocation.getElementType() == ElementType.TYPE ) {
				propertyMetaData.add(
						new ConstrainedType(
								ConfigurationSource.XML,
								constraintsByLocation.get( oneConfiguredLocation ),
								(BeanConstraintLocation) oneConfiguredLocation
						)
				);
			}

		}

		return propertyMetaData;
	}

	/**
	 * @param mappingParser
	 * @param clazz
	 *
	 * @return
	 */
	private Set<BeanConstraintLocation> getCascades(XmlMappingParser mappingParser, Class<?> clazz) {

		Set<BeanConstraintLocation> theValue = newHashSet();

		for ( Member member : mappingParser.getCascadedMembersForClass( clazz ) ) {
			theValue.add( new BeanConstraintLocation( member ) );
		}

		return theValue;
	}

	public AnnotationIgnores getAnnotationIgnores() {
		return annotationIgnores;
	}

	protected Partitioner<ConstraintLocation, MetaConstraint<?>> byLocation() {
		return new Partitioner<ConstraintLocation, MetaConstraint<?>>() {
			public ConstraintLocation getPartition(MetaConstraint<?> constraint) {
				return constraint.getLocation();
			}
		};
	}

}
