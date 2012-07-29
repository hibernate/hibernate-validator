/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.xml;

import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Provides common functionality used within the different XML descriptor
 * parsers.
 *
 * @author Gunnar Morling
 */
public class XmlParserHelper {

	private static final Log log = LoggerFactory.make();
	private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

	/**
	 * Retrieves the value of the "version" attribute of the root element of the
	 * given XML input stream.
	 *
	 * @param resourceName
	 *            The name of the represented XML resource.
	 * @param xmlInputStream
	 *            An input stream representing an XML resource.
	 *
	 * @return The value of the "version" attribute. May be null.
	 */
	public String getVersion(String resourceName, InputStream xmlInputStream) {

		try {
			XMLEventReader xmlEventReader = createXmlEventReader( xmlInputStream );
			StartElement rootElement = getRootElement( xmlEventReader );

			return getVersionValue( rootElement );
		}
		catch ( XMLStreamException e ) {
			throw log.getUnableToDetermineSchemaVersionException( resourceName, e );
		}
	}

	private String getVersionValue(StartElement startElement) {

		if ( startElement == null ) {
			return null;
		}

		Attribute versionAttribute = startElement.getAttributeByName( new QName( "version" ) );
		return versionAttribute != null ? versionAttribute.getValue() : null;
	}

	private StartElement getRootElement(XMLEventReader xmlEventReader) throws XMLStreamException {

		while ( xmlEventReader.hasNext() ) {
			XMLEvent nextEvent = xmlEventReader.nextEvent();
			if ( nextEvent.isStartElement() ) {
				return nextEvent.asStartElement();
			}
		}

		return null;
	}

	private synchronized XMLEventReader createXmlEventReader(InputStream xmlStream) throws XMLStreamException {
		return xmlInputFactory.createXMLEventReader( xmlStream );
	}
}
