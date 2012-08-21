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
package org.hibernate.validator.test.internal.xml;

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
