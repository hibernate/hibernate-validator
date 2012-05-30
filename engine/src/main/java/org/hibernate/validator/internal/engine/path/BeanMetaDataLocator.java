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

import java.util.Iterator;

import javax.validation.Path;

import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;

/**
 * Given a {@code Path} the {@code BeanMetaDataLocator} creates an iterator over
 * all bean meta instances required by this path.
 *
 * @author Hardy Ferentschik
 */
public abstract class BeanMetaDataLocator {
	public static BeanMetaDataLocator createBeanMetaDataLocator(Object rootBean, Class<?> rootBeanClass, BeanMetaDataManager beanMetaDataManager) {
		if ( rootBean == null ) {
			return new BeanMetaDataLocatorClassTraversal( rootBeanClass, beanMetaDataManager  );
		}
		else {
			return new BeanMetaDataInstanceTraversal( rootBean,  beanMetaDataManager );
		}
	}

	public abstract Iterator<BeanMetaData<?>> beanMetaDataIterator(Path path);
}
