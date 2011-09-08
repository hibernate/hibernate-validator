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
package org.hibernate.validator.metadata.descriptor;

import java.lang.reflect.Method;
import java.util.Set;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.metadata.core.MetaConstraint;
import org.hibernate.validator.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.metadata.aggregated.MethodMetaData;
import org.hibernate.validator.metadata.aggregated.PropertyMetaData;
import org.hibernate.validator.method.metadata.MethodDescriptor;
import org.hibernate.validator.method.metadata.TypeDescriptor;
import org.hibernate.validator.util.Contracts;

import static org.hibernate.validator.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.util.Contracts.assertNotNull;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class BeanDescriptorImpl<T> extends ElementDescriptorImpl implements BeanDescriptor, TypeDescriptor {

	public BeanDescriptorImpl(BeanMetaData<T> beanMetaData, Set<MetaConstraint<?>> classLevelConstraints) {
		super( beanMetaData.getBeanClass(), beanMetaData );

		for ( MetaConstraint<?> oneConstraint : classLevelConstraints ) {
			addConstraintDescriptor( oneConstraint.getDescriptor() );
		}
	}

	//BeanDescriptor methods

	public final boolean isBeanConstrained() {
		return getMetaDataBean().getMetaConstraints().size() > 0;
	}

	public final PropertyDescriptor getConstraintsForProperty(String propertyName) {

		assertNotNull( propertyName, "The property name cannot be null" );

		return asPropertyDescriptor( getMetaDataBean().getMetaDataFor( propertyName ) );
	}

	public final Set<PropertyDescriptor> getConstrainedProperties() {

		Set<PropertyDescriptor> theValue = newHashSet();

		Set<PropertyMetaData> propertyMetaData = getMetaDataBean().getAllPropertyMetaData();
		for ( PropertyMetaData oneProperty : propertyMetaData ) {
			if ( oneProperty.isConstrained() ) {
				theValue.add( asPropertyDescriptor( oneProperty ) );
			}
		}

		return theValue;
	}

	//TypeDescriptor methods

	public boolean isTypeConstrained() {

		//are there any bean/property constraints?
		if ( isBeanConstrained() ) {
			return true;
		}

		//are there any method-level constraints?
		for ( MethodMetaData oneMethodMetaData : getMetaDataBean().getAllMethodMetaData() ) {

			if ( oneMethodMetaData.isConstrained() ) {
				return true;
			}
		}

		return false;
	}

	public Set<MethodDescriptor> getConstrainedMethods() {

		BeanMetaData<?> beanMetaData = getMetaDataBean();

		Set<MethodDescriptor> theValue = newHashSet();

		for ( MethodMetaData oneMethodMetaData : beanMetaData.getAllMethodMetaData() ) {
			if ( oneMethodMetaData.isConstrained() ) {
				theValue.add( new MethodDescriptorImpl( beanMetaData, oneMethodMetaData ) );
			}
		}

		return theValue;
	}

	public MethodDescriptor getConstraintsForMethod(String methodName, Class<?>... parameterTypes) {

		Contracts.assertNotNull( methodName, "The method name must not be null" );

		Method method;

		try {
			method = getMetaDataBean().getBeanClass().getMethod( methodName, parameterTypes );
		}
		//No method with the given name/parameter types exists on this type. To be consistent  
		//with getConstraintsForProperty() this is signaled by simply returning null
		catch ( Exception e ) {
			return null;
		}

		return new MethodDescriptorImpl( getMetaDataBean(), getMetaDataBean().getMetaDataFor( method ) );
	}

	public BeanDescriptor getBeanDescriptor() {
		return this;
	}

	//TODO GM: it would be nicer if PropertyMetaData itself could create an equivalent PropertyDescriptor.
	//Currently this doesn't work as the descriptor needs a reference to the BeanMetaData object.
	private PropertyDescriptorImpl asPropertyDescriptor(PropertyMetaData propertyMetaData) {

		if ( propertyMetaData == null || !propertyMetaData.isConstrained() ) {
			return null;
		}

		PropertyDescriptorImpl propertyDescriptor = new PropertyDescriptorImpl(
				propertyMetaData.getType(),
				propertyMetaData.isCascading(),
				propertyMetaData.getPropertyName(),
				getMetaDataBean()
		);

		for ( MetaConstraint<?> oneConstraint : propertyMetaData ) {
			propertyDescriptor.addConstraintDescriptor( oneConstraint.getDescriptor() );
		}

		return propertyDescriptor;
	}

}
