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
package org.hibernate.validator.metadata.descriptor;

import java.util.List;
import java.util.Set;

import org.hibernate.validator.method.metadata.MethodDescriptor;
import org.hibernate.validator.method.metadata.ParameterDescriptor;

/**
 * @author Gunnar Morling
 */
public class MethodDescriptorImpl extends ElementDescriptorImpl implements MethodDescriptor {

	private final String name;
	private final boolean isCascaded;
	private final List<ParameterDescriptor> parameters;

	public MethodDescriptorImpl(Class<?> returnType, String name, boolean isCascaded, Set<ConstraintDescriptorImpl<?>> returnValueConstraints, List<ParameterDescriptor> parameters, boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		super( returnType, returnValueConstraints, defaultGroupSequenceRedefined, defaultGroupSequence );

		this.name = name;
		this.isCascaded = isCascaded;
		this.parameters = parameters;
	}

	public String getMethodName() {
		return name;
	}

	public List<ParameterDescriptor> getParameterDescriptors() {
		return parameters;
	}

	public boolean isCascaded() {
		return isCascaded;
	}

}
