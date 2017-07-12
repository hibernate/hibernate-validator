//tag::include[]
package org.hibernate.validator.referenceguide.chapter09;

//end::include[]

import java.lang.annotation.ElementType;
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.TraversableResolver;

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
