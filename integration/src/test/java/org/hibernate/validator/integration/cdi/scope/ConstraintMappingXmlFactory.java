/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.cdi.scope;

import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;

public class ConstraintMappingXmlFactory {

	public static ByteArrayAsset build(String snippet) {
		StringBuilder sb = new StringBuilder();

		sb.append(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"\n" +
						"<constraint-mappings xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
						"xsi:schemaLocation=\"http://jboss.org/xml/ns/javax/validation/mapping http://jboss.org/xml/ns/javax/validation/mapping/validation-mapping-1.1.xsd\"\n" +
						"xmlns=\"http://jboss.org/xml/ns/javax/validation/mapping\"\n" +
						"                     version=\"1.1\">\n"
		);

		sb.append( snippet );

		sb.append( "\n</constraint-mappings>" );

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
