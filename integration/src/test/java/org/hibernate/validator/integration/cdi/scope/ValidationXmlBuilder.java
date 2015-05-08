/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.cdi.scope;

import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;

public class ValidationXmlBuilder {

	private String constraintMapping;

	public ValidationXmlBuilder constraintMapping(String constraintMapping) {
		this.constraintMapping = constraintMapping;
		return this;
	}

	public ByteArrayAsset build() {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<validation-config xmlns=\"http://jboss.org/xml/ns/javax/validation/configuration\"\n" +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
						"xsi:schemaLocation=\"http://jboss.org/xml/ns/javax/validation/configuration http://jboss.org/xml/ns/javax/validation/configuration/validation-configuration-1.1.xsd\"\n" +
						"version=\"1.1\">\n"
		);

		if ( constraintMapping != null ) {
			sb.append( "  <constraint-mapping>" + constraintMapping + "</constraint-mapping>" );
		}

		sb.append( "</validation-config>" );

		ByteArrayAsset asset = null;
		try {
			String xml = sb.toString();
			asset = new ByteArrayAsset( xml.getBytes( "UTF-8" ) );
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}

		return asset;
	}
}
