/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.property;

import java.lang.reflect.Method;

/**
 * Allows to customize property selection and retrieval based on method instance.
 *
 * Work in progress.
 */
public interface PropertyAccessorSelector {

	MethodType methodType( Method method );

	String propertyName( Method method );

	default boolean isGetterMethod( Method method ) {
		return methodType( method ) == MethodType.PROPERTY_READ_ACCESSOR;
	}

	boolean supports( Method method );

	enum MethodType {
		PROPERTY_READ_ACCESSOR,
		OTHER
	}

}
