/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Hardy Ferentschik
 */
public class ValidationMessages extends ResourceBundle {

	private static final Log log = LoggerFactory.make();

	private static final String DEFAULT_PROPERTIES_FILE_NAME = "/org/hibernate/validator/ValidationMessages.properties";

	private Map<String, String> messages = new HashMap<String, String>();

	public ValidationMessages() throws Exception {

		log.info( "For test purposes are we proxying the built-in messages!" );
		addTestPropertiesToBundle();
		log.infof( "Adding the following properties to default properties %s", messages );

		loadDefaultValidationProperties();
	}

	private void addTestPropertiesToBundle() {
		messages.put( "replace.in.default.bundle1", "{replace.in.default.bundle2}" );
		messages.put( "replace.in.default.bundle2", "foobar" );
	}

	private void loadDefaultValidationProperties() throws IOException {
		InputStream in = this.getClass()
				.getResourceAsStream( DEFAULT_PROPERTIES_FILE_NAME );
		PropertyResourceBundle propertyBundle = new PropertyResourceBundle( in );
		setParent( propertyBundle );
	}

	protected Object handleGetObject(String key) {
		return messages.get( key );
	}

	public Enumeration<String> getKeys() {
		throw new RuntimeException( "Not needed for testing purposes." );
	}
}
