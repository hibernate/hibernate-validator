/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package com.example.money;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import javax.money.spi.ServiceProvider;

/**
 * A {@link ServiceProvider} using a given class loader.
 *
 * @author Guillaume Smet
 */
public class ClassLoaderAwareJavaxMoneyServiceProvider implements ServiceProvider {

	private final ClassLoader classLoader;

	public ClassLoaderAwareJavaxMoneyServiceProvider(ClassLoader externalClassLoader) {
		this.classLoader = externalClassLoader;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public <T> List<T> getServices(final Class<T> serviceType) {
		List<T> services = new ArrayList<>();
		for ( T service : ServiceLoader.load( serviceType, classLoader ) ) {
			services.add( service );
		}

		return services;
	}
}
