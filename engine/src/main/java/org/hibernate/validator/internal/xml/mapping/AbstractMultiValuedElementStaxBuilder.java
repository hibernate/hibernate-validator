/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.hibernate.validator.internal.xml.AbstractStaxBuilder;

/**
 * An abstract builder for an element that could have multiple {@code <value> ... </value>} entries.
 *
 * @author Marko Bekhta
 */
abstract class AbstractMultiValuedElementStaxBuilder extends AbstractStaxBuilder {

	private static final String VALUE_QNAME_LOCAL_PART = "value";

	private static final Class<?>[] EMPTY_CLASSES_ARRAY = new Class<?>[0];

	private final ClassLoadingHelper classLoadingHelper;
	private final DefaultPackageStaxBuilder defaultPackageStaxBuilder;

	private final List<String> values;

	protected AbstractMultiValuedElementStaxBuilder(ClassLoadingHelper classLoadingHelper, DefaultPackageStaxBuilder defaultPackageStaxBuilder) {
		this.classLoadingHelper = classLoadingHelper;
		this.defaultPackageStaxBuilder = defaultPackageStaxBuilder;

		this.values = new ArrayList<>();
	}

	@Override
	protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
		while ( !( xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals( getAcceptableQName() ) ) ) {
			xmlEvent = xmlEventReader.nextEvent();
			if ( xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals( VALUE_QNAME_LOCAL_PART ) ) {
				values.add( readSingleElement( xmlEventReader ) );
			}
		}
	}

	public Class<?>[] build() {
		String defaultPackage = defaultPackageStaxBuilder.build().orElse( "" );
		if ( values.isEmpty() ) {
			return EMPTY_CLASSES_ARRAY;
		}

		return values.stream()
				.map( valueClass -> classLoadingHelper.loadClass( valueClass, defaultPackage ) )
				.peek( this::verifyClass )
				.toArray( Class[]::new );
	}

	public abstract void verifyClass(Class<?> clazz);
}
