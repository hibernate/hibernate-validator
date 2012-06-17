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
package org.hibernate.validator.internal.metadata.descriptor;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.ParameterDescriptor;
import javax.validation.metadata.ReturnValueDescriptor;

/**
 * Describes a validated constructor.
 *
 * @author Gunnar Morling
 */
public class ConstructorDescriptorImpl extends ElementDescriptorImpl implements ConstructorDescriptor {
	private final List<ParameterDescriptor> parameters;
	private final ReturnValueDescriptor returnValueDescriptor;

	public ConstructorDescriptorImpl(Type returnType,
									 Set<ConstraintDescriptorImpl<?>> returnValueConstraints,
									 ReturnValueDescriptor returnValueDescriptor,
									 List<ParameterDescriptor> parameters,
									 boolean defaultGroupSequenceRedefined,
									 List<Class<?>> defaultGroupSequence) {
		super( returnType, returnValueConstraints, defaultGroupSequenceRedefined, defaultGroupSequence );

		this.parameters = Collections.unmodifiableList( parameters );
		this.returnValueDescriptor = returnValueDescriptor;
	}

	@Override
	public List<ParameterDescriptor> getParameterDescriptors() {
		return parameters;
	}

	@Override
	public ReturnValueDescriptor getReturnValueDescriptor() {
		return returnValueDescriptor;
	}

	@Override
	public String toString() {
		return "ConstructorDescriptorImpl [parameters=" + parameters
				+ ", returnValueDescriptor=" + returnValueDescriptor + "]";
	}

	@Override
	public Kind getKind() {
		return Kind.CONSTRUCTOR;
	}
}
