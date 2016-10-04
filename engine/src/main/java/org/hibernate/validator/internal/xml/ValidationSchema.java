/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.validation.ValidationException;

/**
 * Bean Validation schemas reference.
 *
 * @author Guillaume Smet
 */
public enum ValidationSchema {

	VALIDATION_1_0_CONFIGURATION(
			SchemaType.CONFIGURATION,
			"http://jboss.org/xml/ns/javax/validation/configuration",
			"META-INF/validation-configuration-1.0.xsd",
			"1.0"
	),
	VALIDATION_1_0_MAPPING(
			SchemaType.MAPPING,
			"http://jboss.org/xml/ns/javax/validation/mapping",
			"META-INF/validation-mapping-1.0.xsd",
			"1.0"
	),

	VALIDATION_1_1_CONFIGURATION(
			SchemaType.CONFIGURATION,
			"http://jboss.org/xml/ns/javax/validation/configuration",
			"META-INF/validation-configuration-1.1.xsd",
			"1.1"
	),
	VALIDATION_1_1_MAPPING(
			SchemaType.MAPPING,
			"http://jboss.org/xml/ns/javax/validation/mapping",
			"META-INF/validation-mapping-1.1.xsd",
			"1.1"
	),

	VALIDATION_2_0_CONFIGURATION(
			SchemaType.CONFIGURATION,
			"http://xmlns.jcp.org/xml/ns/validation/configuration",
			"META-INF/validation-configuration-2.0.xsd",
			"2.0"
	),
	VALIDATION_2_0_MAPPING(
			SchemaType.MAPPING,
			"http://xmlns.jcp.org/xml/ns/validation/mapping",
			"META-INF/validation-mapping-2.0.xsd",
			"2.0"
	);

	private static final Map<String, ValidationSchema> CONFIGURATION_SCHEMAS_BY_VERSION;
	private static final Map<String, ValidationSchema> MAPPING_SCHEMAS_BY_VERSION;

	static {
		Map<String, ValidationSchema> configurationSchemas = new HashMap<>();
		Map<String, ValidationSchema> mappingSchemas = new HashMap<>();
		for ( ValidationSchema namespace : values() ) {
			switch ( namespace.getSchemaType() ) {
				case CONFIGURATION:
					configurationSchemas.put( namespace.getVersion(), namespace );
					break;
				case MAPPING:
					mappingSchemas.put( namespace.getVersion(), namespace );
					break;
				default:
					throw new ValidationException( "Unsupported schema type " + namespace.getSchemaType().name() );
			}
		}
		CONFIGURATION_SCHEMAS_BY_VERSION = Collections.unmodifiableMap( configurationSchemas );
		MAPPING_SCHEMAS_BY_VERSION = Collections.unmodifiableMap( mappingSchemas );
	}

	private final SchemaType schemaType;

	private final String namespaceURI;

	private final String schemaPath;

	private final String version;

	private ValidationSchema(SchemaType schemaType, String namespaceURI, String schemaPath, String version) {
		this.schemaType = schemaType;
		this.version = version;
		this.schemaPath = schemaPath;
		this.namespaceURI = namespaceURI;
	}

	public SchemaType getSchemaType() {
		return schemaType;
	}

	public String getVersion() {
		return version;
	}

	public String getSchemaPath() {
		return schemaPath;
	}

	public String getNamespaceURI() {
		return namespaceURI;
	}

	public static ValidationSchema getConfigurationSchema(String version) {
		return CONFIGURATION_SCHEMAS_BY_VERSION.get( version );
	}

	public static ValidationSchema getMappingSchema(String version) {
		return MAPPING_SCHEMAS_BY_VERSION.get( version );
	}

	public enum SchemaType {
		CONFIGURATION,
		MAPPING
	}

}
