/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.stax.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.xml.ClassLoadingHelper;
import org.hibernate.validator.internal.xml.stax.AbstractStaxBuilder;

/**
 * A base builder for constraint executables. Provides read logic only. Build logic should be added in implementations.
 *
 * @author Marko Bekhta
 */
abstract class AbstractConstrainedExecutableElementStaxBuilder extends AbstractStaxBuilder {

	private static final QName IGNORE_ANNOTATIONS_QNAME = new QName( "ignore-annotations" );

	protected final ClassLoadingHelper classLoadingHelper;
	protected final ConstraintHelper constraintHelper;
	protected final TypeResolutionHelper typeResolutionHelper;
	protected final ValueExtractorManager valueExtractorManager;
	protected final DefaultPackageStaxBuilder defaultPackageStaxBuilder;
	protected final AnnotationProcessingOptionsImpl annotationProcessingOptions;

	protected String mainAttributeValue;
	protected Optional<Boolean> ignoreAnnotations;
	protected final List<ConstrainedParameterStaxBuilder> constrainedParameterStaxBuilders;
	private CrossParameterStaxBuilder crossParameterStaxBuilder;
	private ReturnValueStaxBuilder returnValueStaxBuilder;

	AbstractConstrainedExecutableElementStaxBuilder(
			ClassLoadingHelper classLoadingHelper, ConstraintHelper constraintHelper,
			TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager,
			DefaultPackageStaxBuilder defaultPackageStaxBuilder, AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		this.classLoadingHelper = classLoadingHelper;
		this.defaultPackageStaxBuilder = defaultPackageStaxBuilder;
		this.constraintHelper = constraintHelper;
		this.typeResolutionHelper = typeResolutionHelper;
		this.valueExtractorManager = valueExtractorManager;

		this.annotationProcessingOptions = annotationProcessingOptions;

		this.constrainedParameterStaxBuilders = new ArrayList<>();
	}

	abstract Optional<QName> getMainAttributeValueQname();

	@Override
	protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
		Optional<QName> mainAttributeValueQname = getMainAttributeValueQname();
		if ( mainAttributeValueQname.isPresent() ) {
			mainAttributeValue = readAttribute( xmlEvent.asStartElement(), mainAttributeValueQname.get() ).get();
		}
		ignoreAnnotations = readAttribute( xmlEvent.asStartElement(), IGNORE_ANNOTATIONS_QNAME ).map( Boolean::parseBoolean );
		ConstrainedParameterStaxBuilder constrainedParameterStaxBuilder = getNewConstrainedParameterStaxBuilder();
		ReturnValueStaxBuilder localReturnValueStaxBuilder = getNewReturnValueStaxBuilder();
		CrossParameterStaxBuilder localCrossParameterStaxBuilder = getNewCrossParameterStaxBuilder();
		while ( !( xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals( getAcceptableQname() ) ) ) {
			xmlEvent = xmlEventReader.nextEvent();
			if ( constrainedParameterStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				constrainedParameterStaxBuilders.add( constrainedParameterStaxBuilder );
				constrainedParameterStaxBuilder = getNewConstrainedParameterStaxBuilder();
			}
			else if ( localReturnValueStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				this.returnValueStaxBuilder = localReturnValueStaxBuilder;
			}
			else if ( localCrossParameterStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				this.crossParameterStaxBuilder = localCrossParameterStaxBuilder;
			}
		}
	}

	private ConstrainedParameterStaxBuilder getNewConstrainedParameterStaxBuilder() {
		return new ConstrainedParameterStaxBuilder( classLoadingHelper, constraintHelper, typeResolutionHelper, valueExtractorManager, defaultPackageStaxBuilder, annotationProcessingOptions );
	}

	private CrossParameterStaxBuilder getNewCrossParameterStaxBuilder() {
		return new CrossParameterStaxBuilder( classLoadingHelper, constraintHelper, typeResolutionHelper, valueExtractorManager, defaultPackageStaxBuilder, annotationProcessingOptions );
	}

	private ReturnValueStaxBuilder getNewReturnValueStaxBuilder() {
		return new ReturnValueStaxBuilder( classLoadingHelper, constraintHelper, typeResolutionHelper, valueExtractorManager, defaultPackageStaxBuilder, annotationProcessingOptions );
	}

	public Optional<ReturnValueStaxBuilder> getReturnValueStaxBuilder() {
		return Optional.ofNullable( returnValueStaxBuilder );
	}

	public Optional<CrossParameterStaxBuilder> geCrossParameterStaxBuilder() {
		return Optional.ofNullable( crossParameterStaxBuilder );
	}
}
