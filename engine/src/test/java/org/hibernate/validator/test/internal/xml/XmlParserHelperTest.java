/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml;

import java.io.InputStream;
import javax.xml.stream.XMLEventReader;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.xml.XmlParserHelper;

import static org.fest.assertions.Assertions.assertThat;

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
	public void shouldRetrieveVersionFor11ValidationXml() {
		InputStream in = XmlParserHelperTest.class.getResourceAsStream( "parameter-name-provider-validation.xml" );
		XMLEventReader xmlEventReader = xmlParserHelper.createXmlEventReader( "META-INF/validation.xml", in );

		String version = xmlParserHelper.getSchemaVersion(
				"META-INF/validation.xml",
				xmlEventReader
		);

		assertThat( version ).isEqualTo( "1.1" );
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
