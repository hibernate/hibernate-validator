/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.cdi.configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import jakarta.validation.ParameterNameProvider;

import org.hibernate.validator.integration.cdi.service.PingService;

/**
 * @author Hardy Ferentschik
 */
public class ParameterNameProviderWithInjection implements ParameterNameProvider {
	@Inject
	private PingService pingService;

	@Override
	public List<String> getParameterNames(Constructor<?> constructor) {
		return Collections.emptyList();
	}

	@Override
	public List<String> getParameterNames(Method method) {
		return Collections.emptyList();
	}
}
