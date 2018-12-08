/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.xml.AbstractStaxBuilder;
import org.hibernate.validator.internal.xml.mapping.ContainerElementTypeConfigurationBuilder.ContainerElementTypeConfiguration;

/**
 * Base builder for all constrained element builders that can have constraints or type argument constraints.
 *
 * @author Marko Bekhta
 */
abstract class AbstractConstrainedElementStaxBuilder extends AbstractStaxBuilder {

	private static final QName IGNORE_ANNOTATIONS_QNAME = new QName( "ignore-annotations" );

	protected final ClassLoadingHelper classLoadingHelper;
	protected final ConstraintCreationContext constraintCreationContext;
	protected final DefaultPackageStaxBuilder defaultPackageStaxBuilder;
	protected final AnnotationProcessingOptionsImpl annotationProcessingOptions;

	protected String mainAttributeValue;
	protected Optional<Boolean> ignoreAnnotations;
	protected final GroupConversionStaxBuilder groupConversionBuilder;
	protected final ValidStaxBuilder validStaxBuilder;
	protected final List<ConstraintTypeStaxBuilder> constraintTypeStaxBuilders;
	protected final ContainerElementTypeConfigurationBuilder containerElementTypeConfigurationBuilder;

	AbstractConstrainedElementStaxBuilder(ClassLoadingHelper classLoadingHelper, ConstraintCreationContext constraintCreationContext,
			DefaultPackageStaxBuilder defaultPackageStaxBuilder, AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		this.classLoadingHelper = classLoadingHelper;
		this.defaultPackageStaxBuilder = defaultPackageStaxBuilder;
		this.constraintCreationContext = constraintCreationContext;

		this.groupConversionBuilder = new GroupConversionStaxBuilder( classLoadingHelper, defaultPackageStaxBuilder );
		this.validStaxBuilder = new ValidStaxBuilder();
		this.containerElementTypeConfigurationBuilder = new ContainerElementTypeConfigurationBuilder();
		this.annotationProcessingOptions = annotationProcessingOptions;

		this.constraintTypeStaxBuilders = new ArrayList<>();
	}

	abstract Optional<QName> getMainAttributeValueQname();

	@Override
	protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
		Optional<QName> mainAttributeValueQname = getMainAttributeValueQname();
		if ( mainAttributeValueQname.isPresent() ) {
			mainAttributeValue = readAttribute( xmlEvent.asStartElement(), mainAttributeValueQname.get() ).get();
		}
		ignoreAnnotations = readAttribute( xmlEvent.asStartElement(), IGNORE_ANNOTATIONS_QNAME ).map( Boolean::parseBoolean );
		ConstraintTypeStaxBuilder constraintTypeStaxBuilder = getNewConstraintTypeStaxBuilder();
		ContainerElementTypeStaxBuilder containerElementTypeStaxBuilder = getNewContainerElementTypeStaxBuilder();
		while ( !( xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals( getAcceptableQName() ) ) ) {
			xmlEvent = xmlEventReader.nextEvent();
			validStaxBuilder.process( xmlEventReader, xmlEvent );
			groupConversionBuilder.process( xmlEventReader, xmlEvent );
			if ( constraintTypeStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				constraintTypeStaxBuilders.add( constraintTypeStaxBuilder );
				constraintTypeStaxBuilder = getNewConstraintTypeStaxBuilder();
			}
			if ( containerElementTypeStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				containerElementTypeConfigurationBuilder.add( containerElementTypeStaxBuilder );
				containerElementTypeStaxBuilder = getNewContainerElementTypeStaxBuilder();
			}
		}
	}

	private ConstraintTypeStaxBuilder getNewConstraintTypeStaxBuilder() {
		return new ConstraintTypeStaxBuilder( classLoadingHelper, constraintCreationContext,
				defaultPackageStaxBuilder );
	}

	private ContainerElementTypeStaxBuilder getNewContainerElementTypeStaxBuilder() {
		return new ContainerElementTypeStaxBuilder( classLoadingHelper, constraintCreationContext,
				defaultPackageStaxBuilder );
	}

	protected ContainerElementTypeConfiguration getContainerElementTypeConfiguration(Type type, ConstraintLocation constraintLocation) {
		return containerElementTypeConfigurationBuilder.build( constraintLocation, type );
	}

	protected CascadingMetaDataBuilder getCascadingMetaData(Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData, Type type) {
		return CascadingMetaDataBuilder.annotatedObject( type, validStaxBuilder.build(), containerElementTypesCascadingMetaData, groupConversionBuilder.build() );
	}
}
