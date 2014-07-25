/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.metadata;

import java.util.EnumSet;

import org.hibernate.validator.util.ConcurrentReferenceHashMap;

import static org.hibernate.validator.util.ConcurrentReferenceHashMap.Option.IDENTITY_COMPARISONS;
import static org.hibernate.validator.util.ConcurrentReferenceHashMap.ReferenceType.SOFT;
import static org.hibernate.validator.util.Contracts.assertNotNull;

/**
 * Cache for created instances of {@code BeanMetaData}.
 *
 * @author Hardy Ferentschik
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class BeanMetaDataCache {
	/**
	 * The default initial capacity for this cache.
	 */
	private static final int DEFAULT_INITIAL_CAPACITY = 16;

	/**
	 * The default load factor for this cache.
	 */
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * The default concurrency level for this cache.
	 */
	private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

	/**
	 * Used to cache the constraint meta data for validated entities
	 */
	private final ConcurrentReferenceHashMap<Class<?>, BeanMetaData<?>> beanMetaDataCache = new ConcurrentReferenceHashMap<Class<?>, BeanMetaData<?>>(
			DEFAULT_INITIAL_CAPACITY,
			DEFAULT_LOAD_FACTOR,
			DEFAULT_CONCURRENCY_LEVEL,
			SOFT,
			SOFT,
			EnumSet.of( IDENTITY_COMPARISONS )
	);

	@SuppressWarnings("unchecked")
	public <T> BeanMetaDataImpl<T> getBeanMetaData(Class<T> beanClass) {
		assertNotNull( beanClass, "Class cannot be null" );

		return (BeanMetaDataImpl<T>) beanMetaDataCache.get( beanClass );
	}

	@SuppressWarnings("unchecked")
	public <T> BeanMetaDataImpl<T> addBeanMetaData(Class<T> beanClass, BeanMetaDataImpl<T> metaData) {
		assertNotNull( beanClass, "Class cannot be null" );
		assertNotNull( metaData, "MetaData cannot be null" );

		return (BeanMetaDataImpl<T>) beanMetaDataCache.putIfAbsent( beanClass, metaData );
	}
}
