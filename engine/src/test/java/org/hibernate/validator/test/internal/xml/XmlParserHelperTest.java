/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml;

import org.hibernate.validator.internal.xml.XmlParserHelper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

		String version = xmlParserHelper.getSchemaVersion(
				"META-INF/validation.xml",
				XmlParserHelperTest.class.getResourceAsStream( "parameter-name-provider-validation.xml" )
		);

		assertThat( version ).isEqualTo( "1.1" );
	}

	@Test
	public void shouldRetrieveVersionFor10ValidationXml() {

		String version = xmlParserHelper.getSchemaVersion(
				"META-INF/validation.xml",
				XmlParserHelperTest.class.getResourceAsStream( "bv-1.0-validation.xml" )
		);

		assertThat( version ).isEqualTo( "1.0" );
	}
}
