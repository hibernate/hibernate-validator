/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter09;

//end::include[]
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import jakarta.validation.ParameterNameProvider;

//tag::include[]
public class MyParameterNameProvider implements ParameterNameProvider {

	@Override
	public List<String> getParameterNames(Constructor<?> constructor) {
		//...
		return null;
	}

	@Override
	public List<String> getParameterNames(Method method) {
		//...
		return null;
	}
}
//end::include[]
