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
package org.hibernate.validator.internal.metadata.descriptor;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.CrossParameterDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.ParameterDescriptor;
import javax.validation.metadata.ReturnValueDescriptor;

/**
 * Describes a validated constructor or method.
 *
 * @author Gunnar Morling
 */
public class ExecutableDescriptorImpl extends ElementDescriptorImpl
		implements ConstructorDescriptor, MethodDescriptor {
	private final String name;
	private final List<ParameterDescriptor> parameters;
	private final CrossParameterDescriptor crossParameterDescriptor;
	private final ReturnValueDescriptor returnValueDescriptor;
	private final boolean isGetter;

	public ExecutableDescriptorImpl(
			Type returnType,
			String name,
			Set<ConstraintDescriptorImpl<?>> crossParameterConstraints,
			ReturnValueDescriptor returnValueDescriptor,
			List<ParameterDescriptor> parameters,
			boolean defaultGroupSequenceRedefined,
			boolean isGetter,
			List<Class<?>> defaultGroupSequence) {
		super(
				returnType,
				Collections.<ConstraintDescriptorImpl<?>>emptySet(),
				defaultGroupSequenceRedefined,
				defaultGroupSequence
		);

		this.name = name;
		this.parameters = Collections.unmodifiableList( parameters );
		this.returnValueDescriptor = returnValueDescriptor;
		this.crossParameterDescriptor = new CrossParameterDescriptorImpl(
				crossParameterConstraints,
				defaultGroupSequenceRedefined,
				defaultGroupSequence
		);
		this.isGetter = isGetter;
	}

	@Override
	public String getName() {
		return name;
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
	public boolean hasConstrainedParameters() {
		if ( crossParameterDescriptor.hasConstraints() ) {
			return true;
		}

		for ( ParameterDescriptor oneParameter : parameters ) {
			if ( oneParameter.hasConstraints() || oneParameter.isCascaded() ) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean hasConstrainedReturnValue() {
		return returnValueDescriptor != null && ( returnValueDescriptor.hasConstraints()
				|| returnValueDescriptor.isCascaded() );
	}

	@Override
	public CrossParameterDescriptor getCrossParameterDescriptor() {
		return crossParameterDescriptor;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ExecutableDescriptorImpl" );
		sb.append( "{name='" ).append( name ).append( '\'' );
		sb.append( '}' );
		return sb.toString();
	}

	public boolean isGetter() {
		return isGetter;
	}
}
