package org.hibernate.validation.impl;

import java.lang.annotation.ElementType;
import javax.validation.TraversableResolver;

/**
 * @author Emmanuel Bernard
 */
public class DefaultTraversableResolver implements TraversableResolver {
	public boolean isTraversable(Object traversableObject, String traversableProperty, Class<?> rootBeanType, String pathToTraversableObject, ElementType elementType) {
		return true;
	}
}
