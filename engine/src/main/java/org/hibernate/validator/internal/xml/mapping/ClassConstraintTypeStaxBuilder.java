/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.xml.AbstractStaxBuilder;

/**
 * Builder for class level constraints.
 *
 * @author Marko Bekhta
 */
class ClassConstraintTypeStaxBuilder extends AbstractStaxBuilder {

	private static final String CLASS_QNAME_LOCAL_PART = "class";
	private static final QName IGNORE_ANNOTATIONS_QNAME = new QName( "ignore-annotations" );

	private final ClassLoadingHelper classLoadingHelper;
	private final ConstraintCreationContext constraintCreationContext;
	private final DefaultPackageStaxBuilder defaultPackageStaxBuilder;
	private final AnnotationProcessingOptionsImpl annotationProcessingOptions;
	private final Map<Class<?>, List<Class<?>>> defaultSequences;

	private Optional<Boolean> ignoreAnnotations;
	private final List<ConstraintTypeStaxBuilder> constraintTypeStaxBuilders;
	private final GroupSequenceStaxBuilder groupSequenceStaxBuilder;

	ClassConstraintTypeStaxBuilder(ClassLoadingHelper classLoadingHelper, ConstraintCreationContext constraintCreationContext,
			DefaultPackageStaxBuilder defaultPackageStaxBuilder, AnnotationProcessingOptionsImpl annotationProcessingOptions,
			Map<Class<?>, List<Class<?>>> defaultSequences) {
		this.classLoadingHelper = classLoadingHelper;
		this.defaultPackageStaxBuilder = defaultPackageStaxBuilder;
		this.constraintCreationContext = constraintCreationContext;

		this.annotationProcessingOptions = annotationProcessingOptions;
		this.defaultSequences = defaultSequences;

		this.constraintTypeStaxBuilders = new ArrayList<>();
		this.groupSequenceStaxBuilder = new GroupSequenceStaxBuilder( classLoadingHelper, defaultPackageStaxBuilder );
	}

	@Override
	protected String getAcceptableQName() {
		return CLASS_QNAME_LOCAL_PART;
	}

	@Override
	protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
		ignoreAnnotations = readAttribute( xmlEvent.asStartElement(), IGNORE_ANNOTATIONS_QNAME ).map( Boolean::parseBoolean );

		ConstraintTypeStaxBuilder constraintTypeStaxBuilder = getNewConstraintTypeStaxBuilder();
		while ( !( xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals( getAcceptableQName() ) ) ) {
			xmlEvent = xmlEventReader.nextEvent();
			groupSequenceStaxBuilder.process( xmlEventReader, xmlEvent );
			if ( constraintTypeStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				constraintTypeStaxBuilders.add( constraintTypeStaxBuilder );
				constraintTypeStaxBuilder = getNewConstraintTypeStaxBuilder();
			}
		}
	}

	private ConstraintTypeStaxBuilder getNewConstraintTypeStaxBuilder() {
		return new ConstraintTypeStaxBuilder( classLoadingHelper, constraintCreationContext,
				defaultPackageStaxBuilder );
	}

	ConstrainedType build(Class<?> beanClass) {
		// group sequence
		List<Class<?>> groupSequence = Arrays.asList( groupSequenceStaxBuilder.build() );
		if ( !groupSequence.isEmpty() ) {
			defaultSequences.put( beanClass, groupSequence );
		}

		// constraints
		ConstraintLocation constraintLocation = ConstraintLocation.forClass( beanClass );

		Set<MetaConstraint<?>> metaConstraints = constraintTypeStaxBuilders.stream()
				.map( builder -> builder.build( constraintLocation, ConstraintLocationKind.TYPE, null ) )
				.collect( Collectors.toSet() );

		// ignore annotation
		if ( ignoreAnnotations.isPresent() ) {
			annotationProcessingOptions.ignoreClassLevelConstraintAnnotations(
					beanClass,
					ignoreAnnotations.get()
			);
		}

		return new ConstrainedType(
				ConfigurationSource.XML,
				beanClass,
				metaConstraints
		);
	}

	private static class GroupSequenceStaxBuilder extends AbstractMultiValuedElementStaxBuilder {

		private static final String GROUP_SEQUENCE_QNAME_LOCAL_PART = "group-sequence";

		private GroupSequenceStaxBuilder(ClassLoadingHelper classLoadingHelper, DefaultPackageStaxBuilder defaultPackageStaxBuilder) {
			super( classLoadingHelper, defaultPackageStaxBuilder );
		}

		@Override
		public void verifyClass(Class<?> clazz) {
			// do nothing
		}

		@Override
		protected String getAcceptableQName() {
			return GROUP_SEQUENCE_QNAME_LOCAL_PART;
		}
	}
}
