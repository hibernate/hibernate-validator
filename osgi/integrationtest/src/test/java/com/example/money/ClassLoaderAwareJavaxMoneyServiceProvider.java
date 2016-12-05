/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package com.example.money;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.money.spi.ServiceProvider;

/**
 * Inspired by the {@code DefaultServiceProvider} provided by javax.money. The only difference is that
 * it uses the provided class loader to load the services.
 *
 * @author Guillaume Smet
 */
public class ClassLoaderAwareJavaxMoneyServiceProvider implements ServiceProvider {

	/** List of services loaded, per class. */
	private final ConcurrentHashMap<Class<?>, List<Object>> servicesLoaded = new ConcurrentHashMap<>();

	private final ClassLoader externalClassLoader;

	public ClassLoaderAwareJavaxMoneyServiceProvider(ClassLoader externalClassLoader) {
		this.externalClassLoader = externalClassLoader;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	/**
	 * Loads and registers services.
	 *
	 * @param serviceType The service type.
	 * @param <T> the concrete type.
	 * @return the items found, never {@code null}.
	 */
	@Override
	public <T> List<T> getServices(final Class<T> serviceType) {
		@SuppressWarnings("unchecked")
		List<T> found = (List<T>) servicesLoaded.get( serviceType );
		if ( found != null ) {
			return found;
		}

		return loadServices( serviceType );
	}

	/**
	 * Loads and registers services.
	 *
	 * @param serviceType The service type.
	 * @param <T> the concrete type.
	 * @return the items found, never {@code null}.
	 */
	private <T> List<T> loadServices(final Class<T> serviceType) {
		List<T> services = new ArrayList<>();
		try {
			for ( T t : ServiceLoader.load( serviceType, externalClassLoader ) ) {
				services.add( t );
			}
			@SuppressWarnings("unchecked")
			final List<T> previousServices = (List<T>) servicesLoaded.putIfAbsent( serviceType, (List<Object>) services );
			return Collections.unmodifiableList( previousServices != null ? previousServices : services );
		}
		catch (Exception e) {
			Logger.getLogger( ClassLoaderAwareJavaxMoneyServiceProvider.class.getName() ).log( Level.WARNING,
					"Error loading services of type " + serviceType, e );
			return services;
		}
	}
}
