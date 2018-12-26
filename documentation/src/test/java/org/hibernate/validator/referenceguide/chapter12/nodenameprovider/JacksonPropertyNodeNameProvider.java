package org.hibernate.validator.referenceguide.chapter12.nodenameprovider;

//tag::include[]
import org.hibernate.validator.spi.nodenameprovider.JavaBeanProperty;
import org.hibernate.validator.spi.nodenameprovider.Property;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

public class JacksonPropertyNodeNameProvider implements PropertyNodeNameProvider {
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
//end::include[]
