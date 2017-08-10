/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.property;

import java.lang.reflect.Method;

/**
 * Allows to customize property selection and retrieval based on concrete method / class
 * <p>
 */
public interface PropertyAccessorSelector {

	String getPropertyName(Method method);

	boolean isGetterMethod(Method method);

	Method findMethod(Class<?> type, String property);

	/**
	 * Allows to have composite (multiple) selectors. Useful in cases when both classic Java Beans and custom POJOs
	 * have to be validated by the same Validator instance.
	 *
	 * Experimental since the exact API has not been decided (perhaps check {@code getPropertyName(method) != null} is sufficient.
	 */
	boolean supports(Method method);


}
