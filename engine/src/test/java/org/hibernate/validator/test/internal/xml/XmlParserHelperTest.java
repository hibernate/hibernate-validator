/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

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
	public void shouldRetrieveVersionFor30ValidationXml() {
		InputStream in = XmlParserHelperTest.class.getResourceAsStream( "parameter-name-provider-validation.xml" );
		XMLEventReader xmlEventReader = xmlParserHelper.createXmlEventReader( "META-INF/validation.xml", in );

		String version = xmlParserHelper.getSchemaVersion(
				"META-INF/validation.xml",
				xmlEventReader
		);

		assertThat( version ).isEqualTo( "3.0" );
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
}
