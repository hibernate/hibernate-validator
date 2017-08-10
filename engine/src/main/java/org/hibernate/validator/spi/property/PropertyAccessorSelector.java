/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.property;

import java.lang.reflect.Method;

/**
 * Allows to customize property naming, selection and retrieval based on reflection metadata.
 *
 * @since 6.1
 */
public interface PropertyAccessorSelector {

	/**
	 * Derives property name from a method. If it is not possible will return {@code null}.
	 *
	 * <p>Example for classic java bean convention: {@code getFoo()} -> {@code foo}</p>
	 *
	 * @param method method to be analyzed
	 * @return property name which is exposed by this method, {@code null} if method is unknown to current
	 * selector instance.
	 */
	String getPropertyName(Method method);

	/**
	 * Checks if current method is a (zero arguments) property accessor (aka "getter")
	 *
	 * @param method method to be analyzed
	 * @return {@code true} if current method can be used to access class property, {@code false} otherwise
	 */
	boolean isGetterMethod(Method method);

	/**
	 * Locates method which is a "getter" for {@code property} in {@code clazz}.
	 *
	 * @param clazz class to be introspected (can't be {@code null})
	 * @param property name of the property (can't be {@code null} or empty)
	 * @return property getter, {@code null} if no such property exists.
	 */
	Method findMethod(Class<?> clazz, String property);

	/**
	 * Allows to have composite (multiple) selectors. Useful in cases when both classic Java Beans and custom POJOs
	 * have to be validated by the same Validator instance.
	 *
	 * Experimental since the exact API has not been decided (perhaps check {@code getPropertyName(method) != null} is sufficient.
	 * TODO perhaps it is redundant
	 *
	 * @param method method to be analyzed
	 *
	 * @return {@code true} if current selector is aware of this method, {@code false} otherwise
	 */
	boolean supports(Method method);


}
