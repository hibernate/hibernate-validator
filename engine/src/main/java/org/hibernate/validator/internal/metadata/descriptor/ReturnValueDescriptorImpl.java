/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.metadata.descriptor;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import jakarta.validation.metadata.ContainerElementTypeDescriptor;
import jakarta.validation.metadata.GroupConversionDescriptor;
import jakarta.validation.metadata.ReturnValueDescriptor;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class ReturnValueDescriptorImpl extends ElementDescriptorImpl
		implements ReturnValueDescriptor {

	@Immutable
	private final Set<ContainerElementTypeDescriptor> constrainedContainerElementTypes;

	private final boolean cascaded;

	@Immutable
	private final Set<GroupConversionDescriptor> groupConversions;

	public ReturnValueDescriptorImpl(Type returnType,
			Set<ConstraintDescriptorImpl<?>> returnValueConstraints,
			Set<ContainerElementTypeDescriptor> constrainedContainerElementTypes,
			boolean cascaded,
			boolean defaultGroupSequenceRedefined,
			List<Class<?>> defaultGroupSequence,
			Set<GroupConversionDescriptor> groupConversions) {
		super(
				returnType,
				returnValueConstraints,
				defaultGroupSequenceRedefined,
				defaultGroupSequence
		);

		this.constrainedContainerElementTypes = CollectionHelper.toImmutableSet( constrainedContainerElementTypes );
		this.cascaded = cascaded;
		this.groupConversions = CollectionHelper.toImmutableSet( groupConversions );
	}

	@Override
	public Set<ContainerElementTypeDescriptor> getConstrainedContainerElementTypes() {
		return constrainedContainerElementTypes;
	}

	@Override
	public boolean isCascaded() {
		return cascaded;
	}

	@Override
	public Set<GroupConversionDescriptor> getGroupConversions() {
		return groupConversions;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ReturnValueDescriptorImpl" );
		sb.append( "{cascaded=" ).append( cascaded );
		sb.append( '}' );
		return sb.toString();
	}
}
