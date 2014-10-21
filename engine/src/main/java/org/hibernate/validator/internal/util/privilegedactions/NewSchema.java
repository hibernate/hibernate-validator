/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.net.URL;
import java.security.PrivilegedExceptionAction;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/**
 * Loads a given XML schema.
 *
 * @author Gunnar Morling
 */
public final class NewSchema implements PrivilegedExceptionAction<Schema> {

	private final SchemaFactory schemaFactory;
	private final URL url;

	public static NewSchema action(SchemaFactory schemaFactory, URL url) {
		return new NewSchema( schemaFactory, url );
	}

	public NewSchema(SchemaFactory schemaFactory, URL url) {
		this.schemaFactory = schemaFactory;
		this.url = url;
	}

	@Override
	public Schema run() throws SAXException {
		return schemaFactory.newSchema( url );
	}
}
