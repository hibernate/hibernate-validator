/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;

import org.hibernate.accessor.HibernateAccessorException;
import org.hibernate.accessor.HibernateAccessorValueReader;
import org.hibernate.validator.internal.engine.path.MutablePath;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.properties.Field;
import org.hibernate.validator.internal.properties.Getter;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * A {@link Cascadable} backed by a property of a Java bean.
 *
 * @author Gunnar Morling
 * @author Marko Bekhta
 */
public abstract class AbstractPropertyCascadable<T extends Property> implements Cascadable {
	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final T property;
	private final HibernateAccessorValueReader<?> propertyAccessor;
	private final Type cascadableType;
	private final CascadingMetaData cascadingMetaData;

	AbstractPropertyCascadable(T property, CascadingMetaData cascadingMetaData) {
		this.property = property;
		this.propertyAccessor = property.createAccessor();
		this.cascadableType = property.getType();
		this.cascadingMetaData = cascadingMetaData;
	}

	@Override
	public Type getCascadableType() {
		return cascadableType;
	}

	@Override
	public Object getValue(Object parent) {
		try {
			return propertyAccessor.get( parent );
		}
		catch (HibernateAccessorException e) {
			throw LOG.getUnexpectedExceptionAccessingBean( e );
		}
	}

	@Override
	public void appendTo(MutablePath path) {
		path.addPropertyNode( property.getResolvedPropertyName() );
	}

	@Override
	public CascadingMetaData getCascadingMetaData() {
		return cascadingMetaData;
	}

	public abstract static class AbstractBuilder<T extends Property> implements Cascadable.Builder {

		private final ValueExtractorManager valueExtractorManager;
		private final T property;
		private CascadingMetaDataBuilder cascadingMetaDataBuilder;

		protected AbstractBuilder(ValueExtractorManager valueExtractorManager, T property, CascadingMetaDataBuilder cascadingMetaDataBuilder) {
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

		protected abstract Cascadable create(T property, CascadingMetaData build);

		public static Cascadable.Builder builder(ValueExtractorManager valueExtractorManager, Property property,
				CascadingMetaDataBuilder cascadingMetaDataBuilder) {
			if ( property instanceof Field ) {
				return new FieldCascadable.Builder( valueExtractorManager, (Field) property, cascadingMetaDataBuilder );
			}
			else if ( property instanceof Getter ) {
				return new GetterCascadable.Builder( valueExtractorManager, (Getter) property, cascadingMetaDataBuilder );
			}
			throw new IllegalStateException( "It should be either a field or a getter." );
		}
	}
}
