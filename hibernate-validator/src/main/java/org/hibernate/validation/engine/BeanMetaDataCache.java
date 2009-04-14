// $Id:$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for created instances of <code>BeanMetaData</code>.
 *
 * @author Hardy Ferentschik
 */
public class BeanMetaDataCache {
	/**
	 * A map for the meta data for each entity. The key is the class and the value the bean meta data for this
	 * entity.
	 */
	private Map<Class<?>, BeanMetaDataImpl<?>> metadataProviders = new ConcurrentHashMap<Class<?>, BeanMetaDataImpl<?>>(
			10
	);

	@SuppressWarnings("unchecked")
	public <T> BeanMetaDataImpl<T> getBeanMetaData(Class<T> beanClass) {
		if ( beanClass == null ) {
			throw new IllegalArgumentException( "Class cannot be null" );
		}
		return ( BeanMetaDataImpl<T> ) metadataProviders.get( beanClass );
	}

	public <T> void addBeanMetaData(Class<T> beanClass, BeanMetaDataImpl<T> metaData) {
		metadataProviders.put( beanClass, metaData );
	}
}
