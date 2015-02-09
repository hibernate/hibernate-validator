/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
 * Allows us to proxy the Hibernate Validator built-in {@code ValidationMessages.properties}.
 * This way parts of the message interpolation algorithm becomes testable.
 *
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
		// see ResourceBundleMessageInterpolatorTest#testRecursiveMessageInterpolation
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
