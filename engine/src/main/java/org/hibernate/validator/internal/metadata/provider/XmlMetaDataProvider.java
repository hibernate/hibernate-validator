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
import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ExecutableConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.util.CollectionHelper.Partitioner;
import org.hibernate.validator.internal.xml.XmlMappingParser;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.CollectionHelper.partition;

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
	public XmlMetaDataProvider(ConstraintHelper constraintHelper, Set<InputStream> mappingStreams) {
		super( constraintHelper );

		XmlMappingParser mappingParser = new XmlMappingParser( constraintHelper );
		mappingParser.parse( mappingStreams );

		for ( Class<?> clazz : mappingParser.getXmlConfiguredClasses() ) {

			Map<ConstraintLocation, Set<MetaConstraint<?>>> constraintsByLocation = partition(
					mappingParser.getConstraintsForClass( clazz ), byLocation()
			);
			Set<ConstraintLocation> cascades = getCascades( mappingParser, clazz );

			Set<ConstrainedElement> constrainedElements = getConstrainedElements( constraintsByLocation, cascades );

			addBeanConfiguration(
					clazz,
					createBeanConfiguration(
							ConfigurationSource.XML,
							clazz,
							constrainedElements,
							mappingParser.getDefaultSequenceForClass( clazz ),
							null
					)
			);
		}

		annotationProcessingOptions = mappingParser.getAnnotationProcessingOptions();
	}

	@Override
	public AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}

	private Set<ConstrainedElement> getConstrainedElements(Map<ConstraintLocation, Set<MetaConstraint<?>>> constraintsByLocation, Set<ConstraintLocation> cascades) {

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
								Collections.<Class<?>, Class<?>>emptyMap(),
								cascades.contains( oneConfiguredLocation )
						)
				);
			}
			else if ( oneConfiguredLocation.getElementType() == ElementType.METHOD ) {
				propertyMetaData.add(
						new ConstrainedExecutable(
								ConfigurationSource.XML,
								new ExecutableConstraintLocation( (Method) oneConfiguredLocation.getMember() ),
								constraintsByLocation.get( oneConfiguredLocation ),
								cascades.contains( oneConfiguredLocation )
						)
				);
			}
			else if ( oneConfiguredLocation.getElementType() == ElementType.TYPE ) {
				propertyMetaData.add(
						new ConstrainedType(
								ConfigurationSource.XML,
								(BeanConstraintLocation) oneConfiguredLocation,
								constraintsByLocation.get( oneConfiguredLocation )
						)
				);
			}

		}

		return propertyMetaData;
	}

	/**
	 * @param mappingParser the xml parser
	 * @param clazz the type for which to retrieve cascaded members
	 *
	 * @return returns a set of cascaded constraints
	 */
	private Set<ConstraintLocation> getCascades(XmlMappingParser mappingParser, Class<?> clazz) {

		Set<ConstraintLocation> cascadedConstraintSet = newHashSet();

		for ( Member member : mappingParser.getCascadedMembersForClass( clazz ) ) {
			cascadedConstraintSet.add( new BeanConstraintLocation( member ) );
		}

		return cascadedConstraintSet;
	}

	protected Partitioner<ConstraintLocation, MetaConstraint<?>> byLocation() {
		return new Partitioner<ConstraintLocation, MetaConstraint<?>>() {
			@Override
			public ConstraintLocation getPartition(MetaConstraint<?> constraint) {
				return constraint.getLocation();
			}
		};
	}
}
