/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.annotation.ElementType;
import java.lang.reflect.Type;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.properties.javabean.JavaBeanField;

/**
 * A {@link Cascadable} backed by a field of a Java bean.
 *
 * @author Gunnar Morling
 * @author Marko Bekhta
 */
public class PropertyCascadable implements Cascadable {

	private final Property property;
	private final Type cascadableType;
	private final CascadingMetaData cascadingMetaData;
	private final ElementType elementType;

	PropertyCascadable(Property property, CascadingMetaData cascadingMetaData) {
		this.property = property;
		this.cascadableType = property.getType();
		this.cascadingMetaData = cascadingMetaData;
		this.elementType = property instanceof JavaBeanField ? ElementType.FIELD : ElementType.METHOD;
	}

	@Override
	public ElementType getElementType() {
		return elementType;
	}

	@Override
	public Type getCascadableType() {
		return cascadableType;
	}

	@Override
	public Object getValue(Object parent) {
		return property.getValueFrom( parent );
	}

	@Override
	public void appendTo(PathImpl path) {
		path.addPropertyNode( property.getPropertyName() );
	}

	@Override
	public CascadingMetaData getCascadingMetaData() {
		return cascadingMetaData;
	}

	public static class Builder implements Cascadable.Builder {

		private final ValueExtractorManager valueExtractorManager;
		private final Property property;
		private CascadingMetaDataBuilder cascadingMetaDataBuilder;

		public Builder(ValueExtractorManager valueExtractorManager, Property property, CascadingMetaDataBuilder cascadingMetaDataBuilder) {
			this.valueExtractorManager = valueExtractorManager;
			this.property = property;
			this.cascadingMetaDataBuilder = cascadingMetaDataBuilder;
		}

		@Override
		public void mergeCascadingMetaData(CascadingMetaDataBuilder cascadingMetaData) {
			this.cascadingMetaDataBuilder = this.cascadingMetaDataBuilder.merge( cascadingMetaData );
		}

		@Override
		public PropertyCascadable build() {
			return new PropertyCascadable( property, cascadingMetaDataBuilder.build( valueExtractorManager, property ) );
		}
	}
}
