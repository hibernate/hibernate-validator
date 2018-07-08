/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.propertyholder.map;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.Map;

import org.hibernate.validator.internal.properties.PropertyAccessor;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.propertyholder.PropertyAccessorCreator;

/**
 * @author Marko Bekhta
 */
public class MapPropertyAccessorCreator implements PropertyAccessorCreator<Map> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	@Override
	public Class<Map> getPropertyHolderType() {
		return Map.class;
	}

	@Override
	public PropertyAccessor create(String propertyName, Type propertyType) {
		return new MapPropertyAccessor( propertyName, propertyType );
	}

	private static class MapPropertyAccessor implements PropertyAccessor {

		private final String name;
		private final Type type;

		private MapPropertyAccessor(String name, Type type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public Object getValueFrom(Object bean) {
			if ( !( bean instanceof Map ) ) {
				throw LOG.getUnexpextedPropertyHolderTypeException( Map.class, bean.getClass() );
			}
			Object value = ( (Map) bean ).get( name );
			if ( value != null && !TypeHelper.isAssignable( type, value.getClass() ) ) {
				throw LOG.getUnexpextedPropertyTypeInPropertyHolderException( type, value.getClass(), name );
			}
			return value;
		}
	}
}
