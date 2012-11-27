/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.engine.path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.validation.Path;

import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.PropertyMetaData;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * @author Hardy Ferentschik
 */
public class BeanMetaDataLocatorClassTraversal extends BeanMetaDataLocator {
	private final Class<?> rootBeanClass;
	private final BeanMetaDataManager beanMetaDataManager;

	BeanMetaDataLocatorClassTraversal(Class<?> rootBeanClass, BeanMetaDataManager beanMetaDataManager) {
		this.rootBeanClass = rootBeanClass;
		this.beanMetaDataManager = beanMetaDataManager;
	}

	@Override
	public Iterator<BeanMetaData<?>> beanMetaDataIterator(Iterator<Path.Node> nodeIterator) {
		Contracts.assertNotNull( nodeIterator );

		List<BeanMetaData<?>> metaDataList = new ArrayList<BeanMetaData<?>>();
		Class<?> currentClass = rootBeanClass;
		while ( nodeIterator.hasNext() ) {
			Path.Node node = nodeIterator.next();
			BeanMetaData<?> beanMetaData = beanMetaDataManager.getBeanMetaData( currentClass );
			metaDataList.add( beanMetaData );

			PropertyMetaData property = beanMetaData.getMetaDataFor( node.getName() );

			if ( property != null && property.isCascading() ) {
				currentClass = property.getRawType();
				if ( ReflectionHelper.isIterable( currentClass ) ) {
					currentClass = (Class<?>) ReflectionHelper.getIndexedType( currentClass );
				}
			}
		}
		return metaDataList.iterator();
	}
}


