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

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.validation.Path;

import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * @author Hardy Ferentschik
 */
public class BeanMetaDataLocatorInstanceTraversal extends BeanMetaDataLocator {
	private final Object rootBean;
	private final BeanMetaDataManager beanMetaDataManager;

	BeanMetaDataLocatorInstanceTraversal(Object rootBean, BeanMetaDataManager beanMetaDataManager) {
		this.rootBean = rootBean;
		this.beanMetaDataManager = beanMetaDataManager;
	}

	@Override
	public Iterator<BeanMetaData<?>> beanMetaDataIterator(Iterator<Path.Node> nodeIterator) {
		Contracts.assertNotNull( nodeIterator );

		List<BeanMetaData<?>> metaDataList = new ArrayList<BeanMetaData<?>>();

		// the path was created as part of the validation, hence we have to be able to access the different
		// involved bean metadata instances
		Object currentValue = rootBean;
		Class<?> currentClass = rootBean.getClass();
		while ( nodeIterator.hasNext() ) {
			Path.Node node = nodeIterator.next();
			BeanMetaData<?> beanMetaData = beanMetaDataManager.getBeanMetaData( currentClass );
			metaDataList.add( beanMetaData );

			Set<Member> members = beanMetaData.getCascadedMembers();
			// TODO - iterating over the members seems to be expensive. Need to check whether we can have a better
			// data structure in BeanMetaData (HF)
			for ( Member member : members ) {
				if ( ReflectionHelper.getPropertyName( member ).equals( node.getName() ) ) {
					currentValue = ReflectionHelper.getValue( member, currentValue );
					if ( currentValue != null ) {
						currentClass = currentValue.getClass();
						Iterator<?> iter = ReflectionHelper.createIteratorForCascadedValue(
								currentClass,
								currentValue
						);
						currentValue = iter.next();
						currentClass = currentValue.getClass();
					}
					break;
				}
			}
		}

		return metaDataList.iterator();
	}
}


