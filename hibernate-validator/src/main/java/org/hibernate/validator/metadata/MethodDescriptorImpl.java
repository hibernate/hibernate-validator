/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.util.List;

import org.hibernate.validator.method.metadata.MethodDescriptor;
import org.hibernate.validator.method.metadata.ParameterDescriptor;

import static org.hibernate.validator.util.CollectionHelper.newArrayList;

/**
 * @author Gunnar Morling
 */
public class MethodDescriptorImpl extends ElementDescriptorImpl implements MethodDescriptor {

	private final AggregatedMethodMetaData methodMetaData;

	public MethodDescriptorImpl(BeanMetaData<?> metaDataBean, AggregatedMethodMetaData methodMetaData) {
		super( methodMetaData.getMethod().getReturnType(), metaDataBean );

		this.methodMetaData = methodMetaData;

		//add the return value constraints of the represented method to the constraint descriptor list
		for ( MethodMetaConstraint<?> oneConstraint : methodMetaData ) {
			addConstraintDescriptor( oneConstraint.getDescriptor() );
		}
	}

	public String getMethodName() {
		return methodMetaData.getMethod().getName();
	}

	public List<ParameterDescriptor> getParameterDescriptors() {

		List<ParameterDescriptor> theValue = newArrayList();

		for ( ParameterMetaData oneParameter : methodMetaData.getAllParameterMetaData() ) {
			theValue.add( new ParameterDescriptorImpl( getMetaDataBean(), oneParameter ) );
		}

		return theValue;
	}

	public boolean isCascaded() {
		return methodMetaData.isCascading();
	}

}
