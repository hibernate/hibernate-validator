/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.hibernate.validator.internal.util.Contracts;

public class HibernateConstraintValidatorInitializationSharedDataManager {
	private final Map<Class<?>, Object> dataMap;

	public HibernateConstraintValidatorInitializationSharedDataManager() {
		this( new HashMap<>() );
	}

	private HibernateConstraintValidatorInitializationSharedDataManager(Map<Class<?>, Object> dataMap) {
		this.dataMap = dataMap;
	}

	public void register(Object data) {
		Contracts.assertNotNull( data, "Data must not be null" );

		dataMap.put( data.getClass(), data );
	}

	public <T, S extends T> void register(Class<T> dataClass, S data) {
		Contracts.assertNotNull( dataClass, "Data class must not be null" );
		Contracts.assertNotNull( data, "Data must not be null" );

		dataMap.put( dataClass, data );
	}

	@SuppressWarnings("unchecked") // because of the way we populate that map
	public <T> T retrieve(Class<T> dataClass) {
		Contracts.assertNotNull( dataClass, "Data class must not be null" );
		return (T) dataMap.get( dataClass );
	}

	@SuppressWarnings("unchecked") // because of the way we populate that map
	public <C, V extends C> C retrieve(Class<C> dataClass, Supplier<V> createIfNotPresent) {
		Contracts.assertNotNull( dataClass, "Data class must not be null" );
		Contracts.assertNotNull( createIfNotPresent, "CreateIfNotPresent must not be null" );

		return (C) dataMap.computeIfAbsent( dataClass, d -> createIfNotPresent.get() );
	}

	public HibernateConstraintValidatorInitializationSharedDataManager copy() {
		return new HibernateConstraintValidatorInitializationSharedDataManager( new ConcurrentHashMap<>( this.dataMap ) );
	}
}
