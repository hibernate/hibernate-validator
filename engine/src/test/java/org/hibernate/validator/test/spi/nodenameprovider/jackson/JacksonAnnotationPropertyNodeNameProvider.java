/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.spi.nodenameprovider.jackson;

import org.hibernate.validator.spi.nodenameprovider.Property;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

public class JacksonAnnotationPropertyNodeNameProvider implements PropertyNodeNameProvider {
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String getName(String propertyName, Property property) {
		JavaType type = objectMapper.constructType( property.getObject().getClass() );
		BeanDescription desc = objectMapper.getSerializationConfig().introspect( type );

		return desc.findProperties()
				.stream()
				.filter( prop -> prop.getInternalName().equals( propertyName ) )
				.map( BeanPropertyDefinition::getName )
				.findFirst()
				.orElse( propertyName );
	}
}
