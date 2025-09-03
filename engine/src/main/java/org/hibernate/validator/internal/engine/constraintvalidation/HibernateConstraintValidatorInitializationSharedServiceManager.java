/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.validator.internal.util.Contracts;

public class HibernateConstraintValidatorInitializationSharedServiceManager {
	private final Map<Class<?>, Object> services;

	public HibernateConstraintValidatorInitializationSharedServiceManager() {
		this( new HashMap<>() );
	}

	private HibernateConstraintValidatorInitializationSharedServiceManager(Map<Class<?>, Object> services) {
		this.services = services;
	}

	public void register(Object service) {
		Contracts.assertNotNull( service, "Service must not be null" );

		services.put( service.getClass(), service );
	}

	public <T, S extends T> void register(Class<T> serviceClass, S service) {
		Contracts.assertNotNull( service, "Service must not be null" );

		services.put( serviceClass, service );
	}

	@SuppressWarnings("unchecked") // because of the way we populate that map
	public <T> T retrieve(Class<T> serviceClass) {
		Contracts.assertNotNull( serviceClass, "Service class must not be null" );
		return (T) services.get( serviceClass );
	}

	public HibernateConstraintValidatorInitializationSharedServiceManager immutableWithDefaultServices(Map<Class<?>, Object> defaultServices) {
		Map<Class<?>, Object> services = new HashMap<>( this.services );
		for ( var entry : defaultServices.entrySet() ) {
			services.putIfAbsent( entry.getKey(), entry.getValue() );
		}
		return new HibernateConstraintValidatorInitializationSharedServiceManager(
				Map.copyOf( services )
		);
	}

	public Collection<Object> registeredServices() {
		return Collections.unmodifiableCollection( services.values() );
	}
}
