/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.reflect.Type;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.properties.Property;

/**
 * A {@link Cascadable} backed by a field of a Java bean.
 *
 * @author Gunnar Morling
 * @author Marko Bekhta
 */
public abstract class PropertyCascadable implements Cascadable {

	private final Property property;
	private final Type cascadableType;
	private final CascadingMetaData cascadingMetaData;

	PropertyCascadable(Property property, CascadingMetaData cascadingMetaData) {
		this.property = property;
		this.cascadableType = property.getType();
		this.cascadingMetaData = cascadingMetaData;
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

	public abstract static class Builder implements Cascadable.Builder {

		private final ValueExtractorManager valueExtractorManager;
		private final Property property;
		private CascadingMetaDataBuilder cascadingMetaDataBuilder;

		protected Builder(ValueExtractorManager valueExtractorManager, Property property, CascadingMetaDataBuilder cascadingMetaDataBuilder) {
			this.valueExtractorManager = valueExtractorManager;
			this.property = property;
			this.cascadingMetaDataBuilder = cascadingMetaDataBuilder;
		}

		@Override
		public void mergeCascadingMetaData(CascadingMetaDataBuilder cascadingMetaData) {
			this.cascadingMetaDataBuilder = this.cascadingMetaDataBuilder.merge( cascadingMetaData );
		}

		@Override
		public Cascadable build() {
			return create( property, cascadingMetaDataBuilder.build( valueExtractorManager, property ) );
		}

		protected abstract Cascadable create(Property property, CascadingMetaData build);

		public static Cascadable.Builder builder(ConstrainedElementKind constrainedElementKind, ValueExtractorManager valueExtractorManager, Property property, CascadingMetaDataBuilder cascadingMetaDataBuilder) {
			if ( ConstrainedElementKind.FIELD == constrainedElementKind ) {
				return new FieldCascadable.Builder( valueExtractorManager, property, cascadingMetaDataBuilder );
			}
			else if ( ConstrainedElementKind.GETTER == constrainedElementKind ) {
				return new GetterCascadable.Builder( valueExtractorManager, property, cascadingMetaDataBuilder );
			}
			throw new IllegalStateException( "It should be either a field or a getter." );
		}
	}
}
