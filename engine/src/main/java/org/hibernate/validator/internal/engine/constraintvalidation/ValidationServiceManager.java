/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.validator.internal.util.Contracts;

public class ValidationServiceManager {
	private final Map<Class<?>, Object> serviceMap;

	public ValidationServiceManager() {
		this( new HashMap<>() );
	}

	private ValidationServiceManager(Map<Class<?>, Object> serviceMap) {
		this.serviceMap = serviceMap;
	}

	public <T> void register(Class<T> serviceType, T service) {
		Contracts.assertNotNull( serviceType, "Service type must not be null" );
		Contracts.assertNotNull( service, "Service must not be null" );

		serviceMap.put( serviceType, service );
	}

	@SuppressWarnings("unchecked")
	public <T> T retrieve(Class<T> serviceType) {
		Contracts.assertNotNull( serviceType, "Service type must not be null" );
		return (T) serviceMap.get( serviceType );
	}

	public ValidationServiceManager seal() {
		return new ValidationServiceManager( Map.copyOf( this.serviceMap ) );
	}
}
