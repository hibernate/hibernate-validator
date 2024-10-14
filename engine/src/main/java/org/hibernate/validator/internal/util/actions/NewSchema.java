/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.actions;

import java.net.URL;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/**
 * Loads a given XML schema.
 *
 * @author Gunnar Morling
 */
public final class NewSchema {

	private NewSchema() {
	}

	public static Schema action(SchemaFactory schemaFactory, URL url) throws SAXException {
		return schemaFactory.newSchema( url );
	}
}
