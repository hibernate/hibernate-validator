/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.property;

import org.hibernate.validator.internal.util.ReflectionHelper;

import java.lang.reflect.Method;

/**
 * Default property accessor which follows java bean naming convention.
 */
public class JavaBeanPropertySelector implements PropertyAccessorSelector {

	@Override
	public MethodType methodType( Method method ) {
		return MethodType.PROPERTY_READ_ACCESSOR;
	}

	@Override
	public String propertyName( Method method ) {
		return ReflectionHelper.getPropertyName( method );
	}

	@Override
	public boolean supports( Method method ) {
		return ReflectionHelper.isGetterMethod( method );
	}

}
