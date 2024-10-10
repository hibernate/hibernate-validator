/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter09;

//end::include[]
import java.lang.annotation.ElementType;

import jakarta.validation.Path;
import jakarta.validation.Path.Node;
import jakarta.validation.TraversableResolver;

//tag::include[]
public class MyTraversableResolver implements TraversableResolver {

	@Override
	public boolean isReachable(
			Object traversableObject,
			Node traversableProperty,
			Class<?> rootBeanType,
			Path pathToTraversableObject,
			ElementType elementType) {
		//...
		return false;
	}

	@Override
	public boolean isCascadable(
			Object traversableObject,
			Node traversableProperty,
			Class<?> rootBeanType,
			Path pathToTraversableObject,
			ElementType elementType) {
		//...
		return false;
	}
}
//end::include[]
