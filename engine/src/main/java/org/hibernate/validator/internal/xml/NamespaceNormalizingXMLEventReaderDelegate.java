/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.EventReaderDelegate;

/**
 * An XML {@link EventReaderDelegate} designed to normalize the XML namespaces.
 * <p>
 * From BV 1.x to BV 2, we changed the namespaces and we need to normalize the namespaces to the ones of BV 2 so that
 * the unmarshaller can do its job.
 * <p>
 * Note: it used to work in JDK 1.8 before the 102 release but the JDK is now stricter:
 * <a href="https://bugs.openjdk.java.net/browse/JDK-8134111">https://bugs.openjdk.java.net/browse/JDK-8134111</a>.
 *
 * @author Guillaume Smet
 */
public class NamespaceNormalizingXMLEventReaderDelegate extends EventReaderDelegate {

	private final XMLEventFactory eventFactory;

	private final Map<String, String> namespaceMapping;

	public NamespaceNormalizingXMLEventReaderDelegate(XMLEventReader eventReader, XMLEventFactory eventFactory, Map<String, String> namespaceMapping) {
		super( eventReader );
		this.eventFactory = eventFactory;
		this.namespaceMapping = namespaceMapping;
	}

	@Override
	public XMLEvent peek() throws XMLStreamException {
		return normalizeXMLEvent( super.peek() );
	}

	@Override
	public XMLEvent nextEvent() throws XMLStreamException {
		return normalizeXMLEvent( super.nextEvent() );
	}

	private XMLEvent normalizeXMLEvent(XMLEvent xmlEvent) {
		if ( xmlEvent.isStartElement() ) {
			return normalizeNamespace( xmlEvent.asStartElement() );
		}
		else if ( xmlEvent.isEndElement() ) {
			return normalizeNamespace( xmlEvent.asEndElement() );
		}
		else {
			return xmlEvent;
		}
	}

	@SuppressWarnings("unchecked")
	private StartElement normalizeNamespace(StartElement element) {
		eventFactory.setLocation( element.getLocation() );
		return eventFactory.createStartElement( normalizeQName( element.getName() ), element.getAttributes(), normalizeNamespaces( element.getNamespaces() ) );
	}

	@SuppressWarnings("unchecked")
	private EndElement normalizeNamespace(EndElement element) {
		eventFactory.setLocation( element.getLocation() );
		return eventFactory.createEndElement( normalizeQName( element.getName() ), normalizeNamespaces( element.getNamespaces() ) );
	}

	private QName normalizeQName(QName qName) {
		return new QName( normalizeNamespaceURI( qName.getNamespaceURI() ), qName.getLocalPart() );
	}

	private Iterator<Namespace> normalizeNamespaces(Iterator<Namespace> namespaces) {
		List<Namespace> newNamespaces = new ArrayList<>();
		while ( namespaces.hasNext() ) {
			newNamespaces.add( normalizeNamespace( namespaces.next() ) );
		}
		return newNamespaces.iterator();
	}

	private Namespace normalizeNamespace(Namespace namespace) {
		return eventFactory.createNamespace( namespace.getPrefix(), normalizeNamespaceURI( namespace.getNamespaceURI() ) );
	}

	private String normalizeNamespaceURI(String namespaceURI) {
		return namespaceMapping.getOrDefault( namespaceURI, namespaceURI );
	}

}
