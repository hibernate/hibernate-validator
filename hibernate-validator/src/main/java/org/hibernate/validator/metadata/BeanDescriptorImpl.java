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

import java.lang.reflect.Method;
import java.util.Set;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.method.metadata.MethodDescriptor;
import org.hibernate.validator.method.metadata.TypeDescriptor;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class BeanDescriptorImpl<T> extends ElementDescriptorImpl implements BeanDescriptor, TypeDescriptor {

	public BeanDescriptorImpl(BeanMetaData<T> beanMetaData) {
		super( beanMetaData.getBeanClass(), beanMetaData );
	}

	//BeanDescriptor methods

	public final boolean isBeanConstrained() {
		return getMetaDataBean().getMetaConstraintsAsMap().size() > 0;
	}

	public final PropertyDescriptor getConstraintsForProperty(String propertyName) {
		if ( propertyName == null ) {
			throw new IllegalArgumentException( "The property name cannot be null" );
		}
		return getMetaDataBean().getPropertyDescriptor( propertyName );
	}

	public final Set<PropertyDescriptor> getConstrainedProperties() {
		return getMetaDataBean().getConstrainedProperties();
	}

	//TypeDescriptor methods

	public boolean isTypeConstrained() {
		throw new UnsupportedOperationException( "Not yet implemented" );
	}

	public Set<MethodDescriptor> getConstrainedMethods() {
		throw new UnsupportedOperationException( "Not yet implemented" );
	}

	public MethodDescriptor getConstraintsForMethod(Method method) {
		throw new UnsupportedOperationException( "Not yet implemented" );
	}

	public BeanDescriptor getBeanDescriptor() {
		return this;
	}
}
