/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.util.Optional;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.hibernate.validator.internal.xml.AbstractStaxBuilder;

/**
 * An abstract builder to be used for reading simple string element that can
 * occur only once in a given block, e.g. {@code <message>} or {@code <default-package>}.
 *
 * @author Marko Bekhta
 */
abstract class AbstractOneLineStringStaxBuilder extends AbstractStaxBuilder {

	private String value;

	@Override
	protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
		this.value = readSingleElement( xmlEventReader );
	}

	public Optional<String> build() {
		return Optional.ofNullable( value );
	}
}
