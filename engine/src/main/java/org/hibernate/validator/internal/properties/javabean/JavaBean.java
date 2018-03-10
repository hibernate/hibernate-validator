/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.javabean;

import java.util.Arrays;
import java.util.stream.Stream;

import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.properties.ConstrainableType;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * @author Marko Bekhta
 */
public class JavaBean implements ConstrainableType {

	private final Class<?> clazz;

	public JavaBean(Class<?> clazz) {
		this.clazz = clazz;
	}

	public Stream<Property> getFieldProperties() {
		return Arrays.stream( clazz.getDeclaredFields() )
				.map( JavaBeanField::new );
	}

	public Stream<Property> getGetterProperties() {
		return Arrays.stream( clazz.getDeclaredMethods() )
				.filter( ReflectionHelper::isGetterMethod )
				.map( JavaBeanGetter::new );
	}
}
