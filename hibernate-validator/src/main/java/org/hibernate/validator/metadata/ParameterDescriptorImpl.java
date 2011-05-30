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

import org.hibernate.validator.method.metadata.ParameterDescriptor;

/**
 * @author Gunnar Morling
 */
public class ParameterDescriptorImpl extends ElementDescriptorImpl implements ParameterDescriptor {

	private ParameterMetaData parameterMetaData;

	public ParameterDescriptorImpl(BeanMetaData<?> metaDataBean, ParameterMetaData parameterMetaData) {
		super( parameterMetaData.getType(), metaDataBean );

		this.parameterMetaData = parameterMetaData;

		//add constraints of the represented parameter to the constraint descriptor list
		for ( MetaConstraint<?> oneConstraint : parameterMetaData ) {
			addConstraintDescriptor( oneConstraint.getDescriptor() );
		}
	}

	public boolean isCascaded() {
		return parameterMetaData.isCascading();
	}

	public int getIndex() {
		return parameterMetaData.getIndex();
	}

}