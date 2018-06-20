/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.javabean.accessors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.hibernate.validator.internal.properties.PropertyAccessor;

/**
 * @author Marko Bekhta
 */
public interface JavaBeanPropertyAccessorFactory {

	static JavaBeanPropertyAccessorFactory of(ConfigurationImpl configuration) {
		if ( configuration == null ) {
			return ReflectionJavaBeanPropertyAccessorFactory.INSTANCE;
		}
		switch ( configuration.getPropertyAccessKind() ) {
			case METHOD_HANDLES:
				return new MethodHandleJavaBeanPropertyAccessorFactory( configuration.getLookup() );
			case REFLECTION:
				return ReflectionJavaBeanPropertyAccessorFactory.INSTANCE;
			default:
				throw new IllegalStateException( "Unsupported property access kind." );
		}
	}

	PropertyAccessor forField(Field field);

	PropertyAccessor forGetter(Method getter);
}
