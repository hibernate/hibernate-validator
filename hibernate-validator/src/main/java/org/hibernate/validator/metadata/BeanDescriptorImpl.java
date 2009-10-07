// $Id$
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

import java.util.Set;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class BeanDescriptorImpl<T> extends ElementDescriptorImpl implements BeanDescriptor {

	public BeanDescriptorImpl(BeanMetaData<T> beanMetaData) {
		super( beanMetaData.getBeanClass(), beanMetaData );
	}

	public boolean isBeanConstrained() {
		return metaDataBean.getMetaConstraintsAsMap().size() > 0;
	}

	public PropertyDescriptor getConstraintsForProperty(String propertyName) {
		if ( propertyName == null ) {
			throw new IllegalArgumentException( "The property name cannot be null" );
		}
		return metaDataBean.getPropertyDescriptor( propertyName );
	}

	public Set<PropertyDescriptor> getConstrainedProperties() {
		return metaDataBean.getConstrainedProperties();
	}
}
