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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.hibernate.validator.util.Contracts.assertNotNull;

/**
 * Cache for created instances of {@code BeanMetaData}.
 *
 * @author Hardy Ferentschik
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class BeanMetaDataCache {
	/**
	 * A map for the meta data for each entity. The key is the class and the value the bean meta data for this
	 * entity.
	 */
	private final ConcurrentMap<Class<?>, BeanMetaDataImpl<?>> metadataProviders = new ConcurrentHashMap<Class<?>, BeanMetaDataImpl<?>>(
			10
	);

	@SuppressWarnings("unchecked")
	public <T> BeanMetaDataImpl<T> getBeanMetaData(Class<T> beanClass) {
		assertNotNull( beanClass, "Class cannot be null" );

		return (BeanMetaDataImpl<T>) metadataProviders.get( beanClass );
	}

	@SuppressWarnings("unchecked")
	public <T> BeanMetaDataImpl<T> addBeanMetaData(Class<T> beanClass, BeanMetaDataImpl<T> metaData) {
		assertNotNull( beanClass, "Class cannot be null" );
		assertNotNull( metaData, "MetaData cannot be null" );

		return (BeanMetaDataImpl<T>) metadataProviders.putIfAbsent( beanClass, metaData );
	}
}
