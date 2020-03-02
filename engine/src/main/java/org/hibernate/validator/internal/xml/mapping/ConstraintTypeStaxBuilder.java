/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.validation.Payload;
import jakarta.validation.ValidationException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.core.MetaConstraints;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;
import org.hibernate.validator.internal.xml.AbstractStaxBuilder;

/**
 * Builder for constraint information. Creates a constraint based on a set of given values.
 *
 * @author Hardy Ferentschik
 * @author Marko Bekhta
 */
class ConstraintTypeStaxBuilder extends AbstractStaxBuilder {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final Pattern IS_ONLY_WHITESPACE = Pattern.compile( "\\s*" );

	private static final String CONSTRAINT_QNAME_LOCAL_PART = "constraint";

	private static final QName CONSTRAINT_ANNOTATION_QNAME = new QName( "annotation" );

	private final ClassLoadingHelper classLoadingHelper;
	private final ConstraintCreationContext constraintCreationContext;
	private final DefaultPackageStaxBuilder defaultPackageStaxBuilder;

	// Builders:
	private final GroupsStaxBuilder groupsStaxBuilder;
	private final PayloadStaxBuilder payloadStaxBuilder;
	private final ConstraintParameterStaxBuilder constrainParameterStaxBuilder;
	private final MessageStaxBuilder messageStaxBuilder;

	private final List<AbstractStaxBuilder> builders;

	private String constraintAnnotation;

	ConstraintTypeStaxBuilder(
			ClassLoadingHelper classLoadingHelper, ConstraintCreationContext constraintCreationContext,
			DefaultPackageStaxBuilder defaultPackageStaxBuilder) {
		this.classLoadingHelper = classLoadingHelper;
		this.defaultPackageStaxBuilder = defaultPackageStaxBuilder;
		this.constraintCreationContext = constraintCreationContext;

		this.groupsStaxBuilder = new GroupsStaxBuilder( classLoadingHelper, defaultPackageStaxBuilder );
		this.payloadStaxBuilder = new PayloadStaxBuilder( classLoadingHelper, defaultPackageStaxBuilder );
		this.constrainParameterStaxBuilder = new ConstraintParameterStaxBuilder( classLoadingHelper, defaultPackageStaxBuilder );
		this.messageStaxBuilder = new MessageStaxBuilder();

		this.builders = Stream.of( groupsStaxBuilder, payloadStaxBuilder, constrainParameterStaxBuilder, messageStaxBuilder )
				.collect( Collectors.collectingAndThen( Collectors.toList(), Collections::unmodifiableList ) );

	}

	@Override
	protected String getAcceptableQName() {
		return CONSTRAINT_QNAME_LOCAL_PART;
	}

	@Override
	protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
		StartElement startElement = xmlEvent.asStartElement();
		constraintAnnotation = readAttribute( startElement, CONSTRAINT_ANNOTATION_QNAME ).get();
		while ( !( xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals( CONSTRAINT_QNAME_LOCAL_PART ) ) ) {
			XMLEvent currentEvent = xmlEvent;
			builders.forEach( builder -> builder.process( xmlEventReader, currentEvent ) );
			xmlEvent = xmlEventReader.nextEvent();
		}
	}

	@SuppressWarnings("unchecked")
	<A extends Annotation> MetaConstraint<A> build(ConstraintLocation constraintLocation, ConstraintLocationKind kind, ConstraintDescriptorImpl.ConstraintType constraintType) {
		String defaultPackage = defaultPackageStaxBuilder.build().orElse( "" );

		Class<A> annotationClass;
		try {
			annotationClass = (Class<A>) classLoadingHelper.loadClass( constraintAnnotation, defaultPackage );
		}
		catch (ValidationException e) {
			throw LOG.getUnableToLoadConstraintAnnotationClassException( constraintAnnotation, e );
		}
		ConstraintAnnotationDescriptor.Builder<A> annotationDescriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( annotationClass );

		// set common things to all constraints:
		Optional<String> message = messageStaxBuilder.build();
		if ( message.isPresent() ) {
			annotationDescriptorBuilder.setMessage( message.get() );
		}
		annotationDescriptorBuilder.setGroups( groupsStaxBuilder.build() )
				.setPayload( payloadStaxBuilder.build() );

		// set constraint specific attributes:
		Map<String, Object> parameters = constrainParameterStaxBuilder.build( annotationClass );
		for ( Map.Entry<String, Object> parameter : parameters.entrySet() ) {
			annotationDescriptorBuilder.setAttribute( parameter.getKey(), parameter.getValue() );
		}

		ConstraintAnnotationDescriptor<A> annotationDescriptor;
		try {
			annotationDescriptor = annotationDescriptorBuilder.build();
		}
		catch (RuntimeException e) {
			throw LOG.getUnableToCreateAnnotationForConfiguredConstraintException( e );
		}

		// we set initially ConstraintOrigin.DEFINED_LOCALLY for all xml configured constraints
		// later we will make copies of this constraint descriptor when needed and adjust the ConstraintOrigin
		ConstraintDescriptorImpl<A> constraintDescriptor = new ConstraintDescriptorImpl<>(
				constraintCreationContext.getConstraintHelper(), constraintLocation.getConstrainable(), annotationDescriptor, kind, constraintType
		);

		return MetaConstraints.create( constraintCreationContext.getTypeResolutionHelper(),
				constraintCreationContext.getValueExtractorManager(),
				constraintCreationContext.getConstraintValidatorManager(), constraintDescriptor, constraintLocation );
	}

	private static class MessageStaxBuilder extends AbstractOneLineStringStaxBuilder {

		private static final String MESSAGE_PACKAGE_QNAME = "message";

		@Override
		protected String getAcceptableQName() {
			return MESSAGE_PACKAGE_QNAME;
		}
	}

	private static class ConstraintParameterStaxBuilder extends AnnotationParameterStaxBuilder {

		private static final String ELEMENT_QNAME_LOCAL_PART = "element";
		private static final QName NAME_QNAME = new QName( "name" );

		public ConstraintParameterStaxBuilder(ClassLoadingHelper classLoadingHelper, DefaultPackageStaxBuilder defaultPackageStaxBuilder) {
			super( classLoadingHelper, defaultPackageStaxBuilder );
		}

		@Override
		protected String getAcceptableQName() {
			return ELEMENT_QNAME_LOCAL_PART;
		}

		@Override
		protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
			String name = readAttribute( xmlEvent.asStartElement(), NAME_QNAME ).get();
			while ( !( xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals( ELEMENT_QNAME_LOCAL_PART ) ) ) {
				xmlEvent = xmlEventReader.nextEvent();
				readElement( xmlEventReader, xmlEvent, name );
			}
		}

		@Override
		protected void checkNameIsValid(String name) {
			if ( ConstraintHelper.MESSAGE.equals( name ) || ConstraintHelper.GROUPS.equals( name ) || ConstraintHelper.PAYLOAD.equals( name ) ) {
				throw LOG.getReservedParameterNamesException( ConstraintHelper.MESSAGE, ConstraintHelper.GROUPS, ConstraintHelper.PAYLOAD );
			}
		}

		public <A extends Annotation> Map<String, Object> build(Class<A> annotationClass) {
			String defaultPackage = defaultPackageStaxBuilder.build().orElse( "" );
			Map<String, Object> builtParameters = new HashMap<>();
			for ( Map.Entry<String, List<String>> parameter : parameters.entrySet() ) {
				builtParameters.put(
						parameter.getKey(),
						getElementValue( parameter.getValue(), annotationClass, parameter.getKey(), defaultPackage )
				);
			}
			for ( Map.Entry<String, List<AnnotationParameterStaxBuilder>> parameter : annotationParameters.entrySet() ) {
				builtParameters.put(
						parameter.getKey(),
						getAnnotationElementValue( parameter.getValue(), annotationClass, parameter.getKey(), defaultPackage )
				);
			}

			return builtParameters;
		}
	}

	private static class AnnotationParameterStaxBuilder extends AbstractStaxBuilder {

		private static final String ANNOTATION_QNAME_LOCAL_PART = "annotation";
		private static final String ELEMENT_QNAME_LOCAL_PART = "element";
		private static final String VALUE_QNAME_LOCAL_PART = "value";
		private static final QName NAME_QNAME = new QName( "name" );

		private final ClassLoadingHelper classLoadingHelper;
		protected final DefaultPackageStaxBuilder defaultPackageStaxBuilder;

		protected Map<String, List<String>> parameters;
		protected Map<String, List<AnnotationParameterStaxBuilder>> annotationParameters;

		public AnnotationParameterStaxBuilder(ClassLoadingHelper classLoadingHelper, DefaultPackageStaxBuilder defaultPackageStaxBuilder) {
			this.classLoadingHelper = classLoadingHelper;
			this.defaultPackageStaxBuilder = defaultPackageStaxBuilder;

			this.parameters = new HashMap<>();
			this.annotationParameters = new HashMap<>();
		}

		@Override
		protected String getAcceptableQName() {
			return ANNOTATION_QNAME_LOCAL_PART;
		}

		@Override
		protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
			while ( !( xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals( ANNOTATION_QNAME_LOCAL_PART ) ) ) {
				xmlEvent = xmlEventReader.nextEvent();
				if ( xmlEvent.isStartElement() ) {
					StartElement startElement = xmlEvent.asStartElement();
					if ( startElement.getName().getLocalPart().equals( ELEMENT_QNAME_LOCAL_PART ) ) {
						String name = readAttribute( xmlEvent.asStartElement(), NAME_QNAME ).get();

						// we put empty collection here in case the corresponding string element in xml is empty
						// if there will be a value it will get merged in this#addParameterValue()
						parameters.put( name, Collections.emptyList() );
						while ( !( xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals( ELEMENT_QNAME_LOCAL_PART ) ) ) {
							readElement( xmlEventReader, xmlEvent, name );
							xmlEvent = xmlEventReader.nextEvent();
						}
					}
				}
			}
		}

		protected void readElement(XMLEventReader xmlEventReader, XMLEvent xmlEvent, String name) throws XMLStreamException {
			// need to check the next element
			if ( xmlEvent.isCharacters() && !xmlEvent.asCharacters().getData().trim().isEmpty() ) {
				// in case it's a value - read it
				StringBuilder stringBuilder = new StringBuilder( xmlEvent.asCharacters().getData() );
				while ( xmlEventReader.peek().isCharacters() ) {
					xmlEvent = xmlEventReader.nextEvent();
					stringBuilder.append( xmlEvent.asCharacters().getData() );
				}
				addParameterValue( name, stringBuilder.toString().trim() );
			}
			else if ( xmlEvent.isStartElement() ) {
				StartElement startElement = xmlEvent.asStartElement();
				// in case of multi-valued parameter read value
				if ( startElement.getName().getLocalPart().equals( VALUE_QNAME_LOCAL_PART ) ) {
					addParameterValue( name, readSingleElement( xmlEventReader ) );
				}
				else if ( startElement.getName().getLocalPart().equals( ANNOTATION_QNAME_LOCAL_PART ) ) {
					addAnnotationParameterValue( name, xmlEventReader, xmlEvent );
				}
			}
		}

		protected void addAnnotationParameterValue(String name, XMLEventReader xmlEventReader, XMLEvent xmlEvent) {
			checkNameIsValid( name );

			AnnotationParameterStaxBuilder annotationParameterStaxBuilder = new AnnotationParameterStaxBuilder( classLoadingHelper, defaultPackageStaxBuilder );
			annotationParameterStaxBuilder.process( xmlEventReader, xmlEvent );

			annotationParameters.merge(
					name,
					Collections.singletonList( annotationParameterStaxBuilder ),
					(v1, v2) -> Stream.concat( v1.stream(), v2.stream() ).collect( Collectors.toList() )
			);
		}

		protected void addParameterValue(String name, String value) {
			checkNameIsValid( name );
			parameters.merge(
					name,
					Collections.singletonList( value ),
					(v1, v2) -> Stream.concat( v1.stream(), v2.stream() ).collect( Collectors.toList() )
			);
		}

		protected void checkNameIsValid(String name) {
			// in case of simple annotation - any name is acceptable
		}

		public <A extends Annotation> Annotation build(Class<A> annotationClass, String defaultPackage) {
			AnnotationDescriptor.Builder<A> annotationDescriptorBuilder = new AnnotationDescriptor.Builder<>( annotationClass );

			for ( Map.Entry<String, List<String>> parameter : parameters.entrySet() ) {
				annotationDescriptorBuilder.setAttribute(
						parameter.getKey(),
						getElementValue( parameter.getValue(), annotationClass, parameter.getKey(), defaultPackage )
				);
			}
			for ( Map.Entry<String, List<AnnotationParameterStaxBuilder>> parameter : annotationParameters.entrySet() ) {
				annotationDescriptorBuilder.setAttribute(
						parameter.getKey(),
						getAnnotationElementValue( parameter.getValue(), annotationClass, parameter.getKey(), defaultPackage )
				);
			}

			return annotationDescriptorBuilder.build().getAnnotation();
		}

		protected <A extends Annotation> Object getElementValue(List<String> parsedParameters, Class<A> annotationClass, String name, String defaultPackage) {
			List<String> parameters = removeEmptyContentElements( parsedParameters );

			Class<?> returnType = getAnnotationParameterType( annotationClass, name );
			boolean isArray = returnType.isArray();
			if ( !isArray ) {
				if ( parameters.size() == 0 ) {
					return "";
				}
				else if ( parameters.size() > 1 ) {
					throw LOG.getAttemptToSpecifyAnArrayWhereSingleValueIsExpectedException();
				}
				return convertStringToReturnType( parameters.get( 0 ), returnType, defaultPackage );
			}
			else {
				return parameters.stream().map( value -> convertStringToReturnType( value, returnType.getComponentType(), defaultPackage ) )
						.toArray( size -> (Object[]) Array.newInstance( returnType.getComponentType(), size ) );
			}
		}

		@SuppressWarnings("unchecked")
		protected <A extends Annotation> Object getAnnotationElementValue(List<AnnotationParameterStaxBuilder> parameters, Class<A> annotationClass, String name, String defaultPackage) {
			Class<?> returnType = getAnnotationParameterType( annotationClass, name );
			boolean isArray = returnType.isArray();
			if ( !isArray ) {
				if ( parameters.size() == 0 ) {
					throw LOG.getEmptyElementOnlySupportedWhenCharSequenceIsExpectedExpection();
				}
				else if ( parameters.size() > 1 ) {
					throw LOG.getAttemptToSpecifyAnArrayWhereSingleValueIsExpectedException();
				}
				return parameters.get( 0 ).build( (Class<Annotation>) returnType, defaultPackage );
			}
			else {
				return parameters.stream().map( value -> value.build( (Class<Annotation>) returnType.getComponentType(), defaultPackage ) )
						.toArray( size -> (Object[]) Array.newInstance( returnType.getComponentType(), size ) );
			}
		}

		private static List<String> removeEmptyContentElements(List<String> params) {
			return params.stream().filter( content -> !IS_ONLY_WHITESPACE.matcher( content ).matches() )
					.collect( Collectors.toList() );
		}

		private static <A extends Annotation> Class<?> getAnnotationParameterType(Class<A> annotationClass, String name) {
			Method m = run( GetMethod.action( annotationClass, name ) );
			if ( m == null ) {
				throw LOG.getAnnotationDoesNotContainAParameterException( annotationClass, name );
			}
			return m.getReturnType();
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private Object convertStringToReturnType(String value, Class<?> returnType, String defaultPackage) {
			Object returnValue;
			if ( returnType == byte.class ) {
				try {
					returnValue = Byte.parseByte( value );
				}
				catch (NumberFormatException e) {
					throw LOG.getInvalidNumberFormatException( "byte", e );
				}
			}
			else if ( returnType == short.class ) {
				try {
					returnValue = Short.parseShort( value );
				}
				catch (NumberFormatException e) {
					throw LOG.getInvalidNumberFormatException( "short", e );
				}
			}
			else if ( returnType == int.class ) {
				try {
					returnValue = Integer.parseInt( value );
				}
				catch (NumberFormatException e) {
					throw LOG.getInvalidNumberFormatException( "int", e );
				}
			}
			else if ( returnType == long.class ) {
				try {
					returnValue = Long.parseLong( value );
				}
				catch (NumberFormatException e) {
					throw LOG.getInvalidNumberFormatException( "long", e );
				}
			}
			else if ( returnType == float.class ) {
				try {
					returnValue = Float.parseFloat( value );
				}
				catch (NumberFormatException e) {
					throw LOG.getInvalidNumberFormatException( "float", e );
				}
			}
			else if ( returnType == double.class ) {
				try {
					returnValue = Double.parseDouble( value );
				}
				catch (NumberFormatException e) {
					throw LOG.getInvalidNumberFormatException( "double", e );
				}
			}
			else if ( returnType == boolean.class ) {
				returnValue = Boolean.parseBoolean( value );
			}
			else if ( returnType == char.class ) {
				if ( value.length() != 1 ) {
					throw LOG.getInvalidCharValueException( value );
				}
				returnValue = value.charAt( 0 );
			}
			else if ( returnType == String.class ) {
				returnValue = value;
			}
			else if ( returnType == Class.class ) {
				returnValue = classLoadingHelper.loadClass( value, defaultPackage );
			}
			else {
				try {
					Class<Enum> enumClass = (Class<Enum>) returnType;
					returnValue = Enum.valueOf( enumClass, value );
				}
				catch (ClassCastException e) {
					throw LOG.getInvalidReturnTypeException( returnType, e );
				}
			}
			return returnValue;
		}

		/**
		 * Runs the given privileged action, using a privileged block if required.
		 *
		 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
		 * privileged actions within HV's protection domain.
		 */
		private static <T> T run(PrivilegedAction<T> action) {
			return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
		}
	}

	private static class GroupsStaxBuilder extends AbstractMultiValuedElementStaxBuilder {

		private static final String GROUPS_QNAME_LOCAL_PART = "groups";

		private GroupsStaxBuilder(ClassLoadingHelper classLoadingHelper, DefaultPackageStaxBuilder defaultPackageStaxBuilder) {
			super( classLoadingHelper, defaultPackageStaxBuilder );
		}

		@Override
		public void verifyClass(Class<?> clazz) {
			// do nothing
		}

		@Override
		protected String getAcceptableQName() {
			return GROUPS_QNAME_LOCAL_PART;
		}
	}

	private static class PayloadStaxBuilder extends AbstractMultiValuedElementStaxBuilder {

		private static final String PAYLOAD_QNAME_LOCAL_PART = "payload";

		private PayloadStaxBuilder(ClassLoadingHelper classLoadingHelper, DefaultPackageStaxBuilder defaultPackageStaxBuilder) {
			super( classLoadingHelper, defaultPackageStaxBuilder );
		}

		@Override
		public void verifyClass(Class<?> payload) {
			if ( !Payload.class.isAssignableFrom( payload ) ) {
				throw LOG.getWrongPayloadClassException( payload );
			}
		}

		@Override
		protected String getAcceptableQName() {
			return PAYLOAD_QNAME_LOCAL_PART;
		}
	}

}
