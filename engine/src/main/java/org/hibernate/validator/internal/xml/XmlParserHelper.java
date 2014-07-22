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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.GetResource;
import org.hibernate.validator.internal.util.privilegedactions.NewSchema;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Provides common functionality used within the different XML descriptor
 * parsers.
 *
 * @author Gunnar Morling
 */
public class XmlParserHelper {

	private static final Log log = LoggerFactory.make();

	/**
	 * The expected number of XML schemas managed by this class. Used to set the
	 * initial cache size.
	 */
	private static final int NUMBER_OF_SCHEMAS = 4;
	private static final String DEFAULT_VERSION = "1.0";

	// xmlInputFactory used to be static in order to cache the factory, but that introduced a leakage of
	// class loader in Wildfly. See HV-842
	private final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

	/**
	 * Read limit for the buffered input stream. Resetting the stream after
	 * reading the version attribute will fail, if this has required reading
	 * more bytes than this limit (1MB) from the stream. Practically, this
	 * should never happen.
	 */
	private static final int READ_LIMIT = 1024 * 1024;

	private static final ConcurrentMap<String, Schema> schemaCache = new ConcurrentHashMap<String, Schema>(
			NUMBER_OF_SCHEMAS
	);

	/**
	 * Retrieves the schema version applying for the given XML input stream as
	 * represented by the "version" attribute of the root element of the stream.
	 *
	 * @param resourceName The name of the represented XML resource.
	 * @param xmlInputStream An input stream representing an XML resource. Must support the
	 * {@link InputStream#mark(int)} and {@link InputStream#reset()}
	 * methods.
	 *
	 * @return The value of the "version" attribute. For compatibility with BV
	 *         1.0, "1.0" will be returned if the given stream doesn't have a
	 *         "version" attribute.
	 */
	public String getSchemaVersion(String resourceName, InputStream xmlInputStream) {
		Contracts.assertNotNull( xmlInputStream, MESSAGES.parameterMustNotBeNull( "xmlInputStream" ) );

		xmlInputStream.mark( READ_LIMIT );

		try {
			XMLEventReader xmlEventReader = createXmlEventReader( xmlInputStream );
			StartElement rootElement = getRootElement( xmlEventReader );

			return getVersionValue( rootElement );
		}
		catch ( XMLStreamException e ) {
			throw log.getUnableToDetermineSchemaVersionException( resourceName, e );
		}
		finally {
			try {
				xmlInputStream.reset();
			}
			catch ( IOException e ) {
				throw log.getUnableToResetXmlInputStreamException( resourceName, e );
			}
		}
	}

	private String getVersionValue(StartElement startElement) {
		if ( startElement == null ) {
			return null;
		}

		Attribute versionAttribute = startElement.getAttributeByName( new QName( "version" ) );
		return versionAttribute != null ? versionAttribute.getValue() : DEFAULT_VERSION;
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

	/**
	 * Returns the XML schema identified by the given resource name.
	 *
	 * @param schemaResource
	 *            the resource name identifying the schema.
	 * @return the schema identified by the given resource name or {@code null} if the resource was not found or could
	 *         not be loaded.
	 */
	Schema getSchema(String schemaResource) {
		Schema schema = schemaCache.get( schemaResource );

		if ( schema != null ) {
			return schema;
		}

		schema = loadSchema( schemaResource );

		if ( schema != null ) {
			Schema previous = schemaCache.putIfAbsent( schemaResource, schema );
			return previous != null ? previous : schema;
		}
		else {
			return null;
		}
	}

	private Schema loadSchema(String schemaResource) {
		ClassLoader loader = run( GetClassLoader.fromClass( XmlParserHelper.class ) );

		URL schemaUrl = run( GetResource.action( loader, schemaResource ) );
		SchemaFactory sf = SchemaFactory.newInstance( javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI );
		Schema schema = null;
		try {
			schema = run( NewSchema.action( sf, schemaUrl ) );
		}
		catch ( Exception e ) {
			log.unableToCreateSchema( schemaResource, e.getMessage() );
		}
		return schema;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}

	private <T> T run(PrivilegedExceptionAction<T> action) throws Exception {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
