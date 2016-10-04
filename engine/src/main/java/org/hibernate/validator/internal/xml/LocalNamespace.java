/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

/**
 * Bean Validation namespaces reference.
 *
 * @author Guillaume Smet
 */
public enum LocalNamespace {
	VALIDATION_1_CONFIGURATION("http://jboss.org/xml/ns/javax/validation/configuration"),
	VALIDATION_1_MAPPING("http://jboss.org/xml/ns/javax/validation/mapping"),

	VALIDATION_2_CONFIGURATION("http://xmlns.jcp.org/xml/ns/validation/configuration"),
	VALIDATION_2_MAPPING("http://xmlns.jcp.org/xml/ns/validation/mapping");

	private String namespaceURI;

	private LocalNamespace(String namespaceURI) {
		this.namespaceURI = namespaceURI;
	}

	public String getNamespaceURI() {
		return namespaceURI;
	}

}
