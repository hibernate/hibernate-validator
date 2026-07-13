/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.stream.XMLEventReader;

import org.hibernate.validator.internal.xml.XmlParserHelper;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit test for {@link XmlParserHelper}.
 *
 * @author Gunnar Morling
 */
public class XmlParserHelperTest {

	private XmlParserHelper xmlParserHelper;

	@BeforeMethod
	public void setupParserHelper() {
		xmlParserHelper = new XmlParserHelper();
	}

	@Test
	public void shouldRetrieveVersionFor31ValidationXml() {
		InputStream in = XmlParserHelperTest.class.getResourceAsStream( "parameter-name-provider-validation.xml" );
		XMLEventReader xmlEventReader = xmlParserHelper.createXmlEventReader( "META-INF/validation.xml", in );

		String version = xmlParserHelper.getSchemaVersion(
				"META-INF/validation.xml",
				xmlEventReader
		);

		assertThat( version ).isEqualTo( "3.1" );
	}

	@Test
	public void shouldRetrieveVersionFor10ValidationXml() {
		InputStream in = XmlParserHelperTest.class.getResourceAsStream( "bv-1.0-validation.xml" );
		XMLEventReader xmlEventReader = xmlParserHelper.createXmlEventReader( "META-INF/validation.xml", in );

		String version = xmlParserHelper.getSchemaVersion(
				"META-INF/validation.xml",
				xmlEventReader
		);

		assertThat( version ).isEqualTo( "1.0" );
	}

	@Test
	public void shouldNotResolveExternalEntitiesWhenReadingSchemaVersion() throws Exception {
		// An external parameter entity in the internal subset is expanded while the DTD is read, which
		// happens before the schema gets a chance to reject the document. Point it at a resource that does
		// not exist: if the parser reaches out for it, reading the version fails. It must be ignored instead.
		Path missing = Files.createTempFile( "hv-external-entity", ".dtd" );
		Files.delete( missing );

		String xml = "<?xml version=\"1.0\"?>\n"
				+ "<!DOCTYPE constraint-mappings [\n"
				+ "  <!ENTITY % ext SYSTEM \"" + missing.toUri() + "\">\n"
				+ "  %ext;\n"
				+ "]>\n"
				+ "<constraint-mappings version=\"3.1\"/>";

		XMLEventReader xmlEventReader = xmlParserHelper.createXmlEventReader(
				"constraint mapping file",
				new ByteArrayInputStream( xml.getBytes( StandardCharsets.UTF_8 ) )
		);

		String version = xmlParserHelper.getSchemaVersion( "constraint mapping file", xmlEventReader );

		assertThat( version ).isEqualTo( "3.1" );
	}
}
