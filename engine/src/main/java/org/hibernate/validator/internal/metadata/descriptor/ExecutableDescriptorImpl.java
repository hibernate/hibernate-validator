/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.descriptor;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.validation.metadata.ConstructorDescriptor;
import jakarta.validation.metadata.CrossParameterDescriptor;
import jakarta.validation.metadata.MethodDescriptor;
import jakarta.validation.metadata.ParameterDescriptor;
import jakarta.validation.metadata.ReturnValueDescriptor;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Describes a validated constructor or method.
 *
 * @author Gunnar Morling
 */
public class ExecutableDescriptorImpl extends ElementDescriptorImpl
		implements ConstructorDescriptor, MethodDescriptor {
	private final String name;
	@Immutable
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
		this.parameters = CollectionHelper.toImmutableList( parameters );
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
