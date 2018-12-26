/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.spi.nodenameprovider.jackson;

import org.hibernate.validator.spi.nodenameprovider.JavaBeanProperty;
import org.hibernate.validator.spi.nodenameprovider.Property;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

/**
 * An example of how a name can be resolved from a Jackson annotation.
 *
 * @author Damir Alibegovic
 */
public class JacksonAnnotationPropertyNodeNameProvider implements PropertyNodeNameProvider {
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String getName(Property property) {
		if ( property instanceof JavaBeanProperty ) {
			return getJavaBeanPropertyName( (JavaBeanProperty) property );
		}

		return getDefaultName( property );
	}

	private String getJavaBeanPropertyName(JavaBeanProperty property) {
		JavaType type = objectMapper.constructType( property.getDeclaringClass() );
		BeanDescription desc = objectMapper.getSerializationConfig().introspect( type );

		return desc.findProperties()
				.stream()
				.filter( prop -> prop.getInternalName().equals( property.getName() ) )
				.map( BeanPropertyDefinition::getName )
				.findFirst()
				.orElse( property.getName() );
	}

	private String getDefaultName(Property property) {
		return property.getName();
	}
}
