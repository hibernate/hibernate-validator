/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.cdi.configuration;

import java.lang.annotation.ElementType;
import javax.inject.Inject;
import jakarta.validation.Path;
import jakarta.validation.TraversableResolver;

import org.hibernate.validator.integration.cdi.service.PingService;

/**
 * @author Hardy Ferentschik
 */
public class TraversableResolverWithInjection implements TraversableResolver {
	@Inject
	private PingService pingService;

	@Override
	public boolean isReachable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
		return false;
	}

	@Override
	public boolean isCascadable(Object traversableObject, Path.Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
		return false;
	}

	public PingService getPingService() {
		return pingService;
	}
}
