/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
