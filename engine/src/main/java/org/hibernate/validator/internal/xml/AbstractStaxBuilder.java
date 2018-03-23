/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import java.util.Optional;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Other Stax xml builders should extend from this one.
 * Provides some common functionality like reading an attribute value
 * or value of a simple tag.
 *
 * @author Marko Bekhta
 */
public abstract class AbstractStaxBuilder {

	protected abstract String getAcceptableQName();

	/**
	 * Checks if the given {@link XMLEvent} is a {@link StartElement} and if the
	 * corresponding xml tag can be processed based on a tag name.
	 *
	 * @param xmlEvent an event to check
	 *
	 * @return {@code true} if corresponding event can be processed by current builder,
	 * 		{@code false} otherwise
	 */
	protected boolean accept(XMLEvent xmlEvent) {
		return xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals( getAcceptableQName() );
	}

	public boolean process(XMLEventReader xmlEventReader, XMLEvent xmlEvent) {
		if ( accept( xmlEvent ) ) {
			try {
				add( xmlEventReader, xmlEvent );
			}
			catch (XMLStreamException e) {
				throw new IllegalStateException( e );
			}
			return true;
		}
		return false;
	}

	protected abstract void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException;

	/**
	 * Reads a value between a simple tag element. In case of a {@code <someTag>some-value</someTag>} will
	 * return {@code some-value} as a string.
	 *
	 * @param xmlEventReader a current {@link XMLEventReader}
	 *
	 * @return a value of a current xml tag as a string
	 */
	protected String readSingleElement(XMLEventReader xmlEventReader) throws XMLStreamException {
		// trimming the string value as it might contain leading/trailing spaces or \n
		XMLEvent xmlEvent = xmlEventReader.nextEvent();
		StringBuilder stringBuilder = new StringBuilder( xmlEvent.asCharacters().getData() );
		while ( xmlEventReader.peek().isCharacters() ) {
			xmlEvent = xmlEventReader.nextEvent();
			stringBuilder.append( xmlEvent.asCharacters().getData() );
		}
		return stringBuilder.toString().trim();
	}

	/**
	 * Reads a value of an attribute of a given element.
	 *
	 * @param startElement an element to get an attribute from
	 * @param qName a {@link QName} of an attribute to read
	 *
	 * @return a value of an attribute if it is present, {@link Optional#empty()} otherwise
	 */
	protected Optional<String> readAttribute(StartElement startElement, QName qName) {
		Attribute attribute = startElement.getAttributeByName( qName );
		return Optional.ofNullable( attribute ).map( Attribute::getValue );
	}
}
