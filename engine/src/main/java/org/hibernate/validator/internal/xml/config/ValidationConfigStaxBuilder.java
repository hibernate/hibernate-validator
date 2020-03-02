/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.config;

import java.lang.invoke.MethodHandles;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.BootstrapConfiguration;
import jakarta.validation.executable.ExecutableType;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.xml.AbstractStaxBuilder;

/**
 * Stax parser/builder for {@code validation.xml} that reads {@code <validation-config>}
 * information and creates {@link BootstrapConfiguration}.
 *
 * @author Marko Bekhta
 */
class ValidationConfigStaxBuilder extends AbstractStaxBuilder {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final String VALIDATION_CONFIG_QNAME = "validation-config";

	private final SimpleConfigurationsStaxBuilder simpleConfigurationsStaxBuilder = new SimpleConfigurationsStaxBuilder();
	private final PropertyStaxBuilder propertyStaxBuilder = new PropertyStaxBuilder();
	private final ValueExtractorsStaxBuilder valueExtractorsStaxBuilder = new ValueExtractorsStaxBuilder();
	private final ConstraintMappingsStaxBuilder constraintMappingsStaxBuilder = new ConstraintMappingsStaxBuilder();
	private final ExecutableValidationStaxBuilder executableValidationStaxBuilder = new ExecutableValidationStaxBuilder();

	private final Map<String, AbstractStaxBuilder> builders;

	public ValidationConfigStaxBuilder(XMLEventReader xmlEventReader) throws XMLStreamException {
		builders = new HashMap<>();
		builders.put( propertyStaxBuilder.getAcceptableQName(), propertyStaxBuilder );
		builders.put( valueExtractorsStaxBuilder.getAcceptableQName(), valueExtractorsStaxBuilder );
		builders.put( constraintMappingsStaxBuilder.getAcceptableQName(), constraintMappingsStaxBuilder );
		builders.put( executableValidationStaxBuilder.getAcceptableQName(), executableValidationStaxBuilder );
		for ( String name : SimpleConfigurationsStaxBuilder.getProcessedElementNames() ) {
			builders.put( name, simpleConfigurationsStaxBuilder );
		}

		while ( xmlEventReader.hasNext() ) {
			process( xmlEventReader, xmlEventReader.nextEvent() );
		}
	}

	@Override
	protected String getAcceptableQName() {
		return VALIDATION_CONFIG_QNAME;
	}

	@Override
	protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
		while ( !( xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals( VALIDATION_CONFIG_QNAME ) ) ) {
			XMLEvent currentEvent = xmlEventReader.nextEvent();
			xmlEvent = currentEvent;
			if ( currentEvent.isStartElement() ) {
				StartElement startElement = currentEvent.asStartElement();
				String localPart = startElement.getName().getLocalPart();
				AbstractStaxBuilder builder = builders.get( localPart );
				if ( builder != null ) {
					builder.process( xmlEventReader, xmlEvent );
				}
				else {
					LOG.logUnknownElementInXmlConfiguration( localPart );
				}
			}
		}
	}

	public BootstrapConfiguration build() {
		Map<String, String> properties = propertyStaxBuilder.build();
		return new BootstrapConfigurationImpl(
				simpleConfigurationsStaxBuilder.getDefaultProvider(),
				simpleConfigurationsStaxBuilder.getConstraintValidatorFactory(),
				simpleConfigurationsStaxBuilder.getMessageInterpolator(),
				simpleConfigurationsStaxBuilder.getTraversableResolver(),
				simpleConfigurationsStaxBuilder.getParameterNameProvider(),
				simpleConfigurationsStaxBuilder.getClockProvider(),
				valueExtractorsStaxBuilder.build(),
				executableValidationStaxBuilder.build(),
				executableValidationStaxBuilder.isEnabled(),
				constraintMappingsStaxBuilder.build(),
				properties
		);
	}

	private static class SimpleConfigurationsStaxBuilder extends AbstractStaxBuilder {

		/**
		 * Single occurrence elements:
		 */
		private static final String DEFAULT_PROVIDER = "default-provider";
		private static final String MESSAGE_INTERPOLATOR = "message-interpolator";
		private static final String TRAVERSABLE_RESOLVER = "traversable-resolver";
		private static final String CONSTRAINT_VALIDATOR_FACTORY = "constraint-validator-factory";
		private static final String PARAMETER_NAME_PROVIDER = "parameter-name-provider";
		private static final String CLOCK_PROVIDER = "clock-provider";

		private static final Set<String> SINGLE_ELEMENTS = CollectionHelper.toImmutableSet(
				CollectionHelper.asSet(
						DEFAULT_PROVIDER, MESSAGE_INTERPOLATOR, TRAVERSABLE_RESOLVER,
						CONSTRAINT_VALIDATOR_FACTORY, PARAMETER_NAME_PROVIDER, CLOCK_PROVIDER
				)
		);

		/**
		 * Map that contains any of the {@link this#SINGLE_ELEMENTS} elements.
		 */
		private final Map<String, String> singleValuedElements = new HashMap<>();

		@Override
		protected String getAcceptableQName() {
			throw new UnsupportedOperationException( "this method shouldn't be called" );
		}

		@Override
		protected boolean accept(XMLEvent xmlEvent) {
			return xmlEvent.isStartElement()
					&& SINGLE_ELEMENTS.contains( xmlEvent.asStartElement().getName().getLocalPart() );
		}

		@Override
		protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
			String localPart = xmlEvent.asStartElement().getName().getLocalPart();
			singleValuedElements.put( localPart, readSingleElement( xmlEventReader ) );
		}

		public String getDefaultProvider() {
			return singleValuedElements.get( DEFAULT_PROVIDER );
		}

		public String getMessageInterpolator() {
			return singleValuedElements.get( MESSAGE_INTERPOLATOR );
		}

		public String getTraversableResolver() {
			return singleValuedElements.get( TRAVERSABLE_RESOLVER );
		}

		public String getClockProvider() {
			return singleValuedElements.get( CLOCK_PROVIDER );
		}

		public String getConstraintValidatorFactory() {
			return singleValuedElements.get( CONSTRAINT_VALIDATOR_FACTORY );
		}

		public String getParameterNameProvider() {
			return singleValuedElements.get( PARAMETER_NAME_PROVIDER );
		}

		public static Set<String> getProcessedElementNames() {
			return SINGLE_ELEMENTS;
		}
	}

	private static class PropertyStaxBuilder extends AbstractStaxBuilder {

		private static final String PROPERTY_QNAME_LOCAL_PART = "property";
		private static final QName NAME_QNAME = new QName( "name" );

		private final Map<String, String> properties;

		private PropertyStaxBuilder() {
			properties = new HashMap<>();
		}

		@Override
		protected String getAcceptableQName() {
			return PROPERTY_QNAME_LOCAL_PART;
		}

		@Override
		protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
			StartElement startElement = xmlEvent.asStartElement();
			String name = readAttribute( startElement, NAME_QNAME ).get();
			String value = readSingleElement( xmlEventReader );
			if ( LOG.isDebugEnabled() ) {
				LOG.debugf(
						"Found property '%s' with value '%s' in validation.xml.",
						name,
						value
				);
			}
			properties.put( name, value );
		}

		public Map<String, String> build() {
			return properties;
		}
	}

	private static class ValueExtractorsStaxBuilder extends AbstractStaxBuilder {

		private static final String VALUE_EXTRACTOR_QNAME_LOCAL_PART = "value-extractor";

		private final Set<String> valueExtractors = new HashSet<>();

		@Override
		protected String getAcceptableQName() {
			return VALUE_EXTRACTOR_QNAME_LOCAL_PART;
		}

		@Override
		protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
			String value = readSingleElement( xmlEventReader );
			if ( !valueExtractors.add( value ) ) {
				throw LOG.getDuplicateDefinitionsOfValueExtractorException( value );
			}
		}

		public Set<String> build() {
			return valueExtractors;
		}
	}

	private static class ConstraintMappingsStaxBuilder extends AbstractStaxBuilder {

		private static final String CONSTRAINT_MAPPING_QNAME_LOCAL_PART = "constraint-mapping";

		private final Set<String> constraintMappings = new HashSet<>();

		@Override
		protected String getAcceptableQName() {
			return CONSTRAINT_MAPPING_QNAME_LOCAL_PART;
		}

		@Override
		protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
			String value = readSingleElement( xmlEventReader );
			constraintMappings.add( value );
		}

		public Set<String> build() {
			return constraintMappings;
		}
	}

	private static class ExecutableValidationStaxBuilder extends AbstractStaxBuilder {

		private static final String EXECUTABLE_VALIDATION_QNAME_LOCAL_PART = "executable-validation";
		private static final String EXECUTABLE_TYPE_QNAME_LOCAL_PART = "executable-type";

		private static final QName ENABLED_QNAME = new QName( "enabled" );

		private Boolean enabled;

		private EnumSet<ExecutableType> executableTypes = EnumSet.noneOf( ExecutableType.class );

		@Override
		protected String getAcceptableQName() {
			return EXECUTABLE_VALIDATION_QNAME_LOCAL_PART;
		}

		@Override
		protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
			Optional<String> enabledAttribute = readAttribute( xmlEvent.asStartElement(), ENABLED_QNAME );
			if ( enabledAttribute.isPresent() ) {
				enabled = Boolean.parseBoolean( enabledAttribute.get() );
			}

			while ( !( xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals( EXECUTABLE_VALIDATION_QNAME_LOCAL_PART ) ) ) {
				XMLEvent currentEvent = xmlEventReader.nextEvent();
				xmlEvent = currentEvent;
				if ( currentEvent.isStartElement() && currentEvent.asStartElement().getName().getLocalPart().equals( EXECUTABLE_TYPE_QNAME_LOCAL_PART ) ) {
					executableTypes.add( ExecutableType.valueOf( readSingleElement( xmlEventReader ) ) );
				}
			}
		}

		public boolean isEnabled() {
			return enabled == null ? true : enabled;
		}

		/**
		 * Returns an enum set with the executable types corresponding to the given
		 * XML configuration, considering the special elements
		 * {@link ExecutableType#ALL} and {@link ExecutableType#NONE}.
		 *
		 * @return An enum set representing the given executable types.
		 */
		public EnumSet<ExecutableType> build() {
			return executableTypes.isEmpty() ? null : executableTypes;
		}
	}
}
