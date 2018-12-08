/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.xml.AbstractStaxBuilder;

/**
 * Builder for cross parameters.
 *
 * @author Marko Bekhta
 */
class CrossParameterStaxBuilder extends AbstractStaxBuilder {

	private static final String CROSS_PARAMETER_QNAME_LOCAL_PART = "cross-parameter";
	private static final QName IGNORE_ANNOTATIONS_QNAME = new QName( "ignore-annotations" );

	protected final ClassLoadingHelper classLoadingHelper;
	protected final ConstraintCreationContext constraintCreationContext;
	protected final DefaultPackageStaxBuilder defaultPackageStaxBuilder;
	protected final AnnotationProcessingOptionsImpl annotationProcessingOptions;

	protected Optional<Boolean> ignoreAnnotations;
	protected final List<ConstraintTypeStaxBuilder> constraintTypeStaxBuilders;

	CrossParameterStaxBuilder(ClassLoadingHelper classLoadingHelper, ConstraintCreationContext constraintCreationContext,
			DefaultPackageStaxBuilder defaultPackageStaxBuilder, AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		this.classLoadingHelper = classLoadingHelper;
		this.defaultPackageStaxBuilder = defaultPackageStaxBuilder;
		this.constraintCreationContext = constraintCreationContext;

		this.annotationProcessingOptions = annotationProcessingOptions;

		this.constraintTypeStaxBuilders = new ArrayList<>();
	}

	@Override
	protected String getAcceptableQName() {
		return CROSS_PARAMETER_QNAME_LOCAL_PART;
	}

	@Override
	protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
		ignoreAnnotations = readAttribute( xmlEvent.asStartElement(), IGNORE_ANNOTATIONS_QNAME ).map( Boolean::parseBoolean );
		ConstraintTypeStaxBuilder constraintTypeStaxBuilder = getNewConstraintTypeStaxBuilder();
		while ( !( xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals( getAcceptableQName() ) ) ) {
			xmlEvent = xmlEventReader.nextEvent();
			if ( constraintTypeStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				constraintTypeStaxBuilders.add( constraintTypeStaxBuilder );
				constraintTypeStaxBuilder = getNewConstraintTypeStaxBuilder();
			}
		}
	}

	private ConstraintTypeStaxBuilder getNewConstraintTypeStaxBuilder() {
		return new ConstraintTypeStaxBuilder( classLoadingHelper, constraintCreationContext, defaultPackageStaxBuilder );
	}

	Set<MetaConstraint<?>> build(Callable callable) {

		ConstraintLocation constraintLocation = ConstraintLocation.forCrossParameter( callable );

		Set<MetaConstraint<?>> crossParameterConstraints = constraintTypeStaxBuilders.stream()
				.map( builder -> builder.build( constraintLocation, ConstraintLocationKind.of( callable.getConstrainedElementKind() ),
						ConstraintType.CROSS_PARAMETER ) )
				.collect( Collectors.toSet() );

		// ignore annotations
		if ( ignoreAnnotations.isPresent() ) {
			annotationProcessingOptions.ignoreConstraintAnnotationsForCrossParameterConstraint(
					callable,
					ignoreAnnotations.get()
			);
		}

		return crossParameterConstraints;
	}
}
