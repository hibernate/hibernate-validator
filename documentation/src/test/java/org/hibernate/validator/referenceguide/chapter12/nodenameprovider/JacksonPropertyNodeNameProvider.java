/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter12.nodenameprovider;

import java.util.List;

import org.hibernate.validator.spi.nodenameprovider.JavaBeanProperty;
import org.hibernate.validator.spi.nodenameprovider.Property;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProviderContext;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import tools.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;

//tag::include[]
public class JacksonPropertyNodeNameProvider implements PropertyNodeNameProvider {
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String getName(Property property, PropertyNodeNameProviderContext context) {
		if ( property instanceof JavaBeanProperty javaBeanProperty ) {
			var visitor = new JsonPropertyNameGetter( javaBeanProperty, context );
			try {
				objectMapper.acceptJsonFormatVisitor(
						javaBeanProperty.getDeclaringClass(),
						visitor
				);
				var attributeName = visitor.getAttributeName();
				return attributeName != null ? attributeName : property.getName();
			}
			catch (JacksonException ignored) {
			}
		}
		return property.getName();
	}

	private static class JsonPropertyNameGetter extends JsonFormatVisitorWrapper.Base {

		private final JsonObjectFormatVisitor visitor;
		private String attributeName = null;

		JsonPropertyNameGetter(JavaBeanProperty property, PropertyNodeNameProviderContext context) {
			String memberName = property.getMemberName();
			String name = property.getName();
			List<String> nameCandidates = context.getGetterPropertySelectionStrategy().getGetterMethodNameCandidates( name );
			this.visitor = new JsonObjectFormatVisitor.Base( getContext() ) {

				@Override
				public void property(BeanProperty prop) {
					setAttributeName( prop );
				}

				@Override
				public void optionalProperty(BeanProperty prop) {
					setAttributeName( prop );
				}

				private void setAttributeName(BeanProperty writer) {
					if ( memberName.equalsIgnoreCase( writer.getMember().getName() )
							|| name.equalsIgnoreCase( writer.getMember().getName() )
							|| nameCandidates.contains( writer.getMember().getName() ) ) {
						attributeName = writer.getName();
					}
				}
			};
		}

		public String getAttributeName() {
			return attributeName;
		}

		@Override
		public JsonObjectFormatVisitor expectObjectFormat(JavaType type) {
			return visitor;
		}
	}
}
//end::include[]
