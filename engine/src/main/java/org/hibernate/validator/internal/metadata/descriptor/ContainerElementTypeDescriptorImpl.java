/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.descriptor;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import jakarta.validation.metadata.ContainerElementTypeDescriptor;
import jakarta.validation.metadata.GroupConversionDescriptor;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

public class ContainerElementTypeDescriptorImpl extends ElementDescriptorImpl implements ContainerElementTypeDescriptor {

	private final Class<?> containerClass;

	private final Integer typeArgumentIndex;

	@Immutable
	private final Set<ContainerElementTypeDescriptor> constrainedContainerElementTypes;

	private final boolean cascaded;

	@Immutable
	private final Set<GroupConversionDescriptor> groupConversions;

	public ContainerElementTypeDescriptorImpl(Type type,
			Class<?> containerClass,
			Integer typeArgumentIndex,
			Set<ConstraintDescriptorImpl<?>> constraints,
			Set<ContainerElementTypeDescriptor> constrainedContainerElementTypes,
			boolean cascaded,
			boolean defaultGroupSequenceRedefined,
			List<Class<?>> defaultGroupSequence,
			Set<GroupConversionDescriptor> groupConversions) {
		super( type, constraints, defaultGroupSequenceRedefined, defaultGroupSequence );

		this.containerClass = containerClass;
		this.typeArgumentIndex = typeArgumentIndex;
		this.constrainedContainerElementTypes = CollectionHelper.toImmutableSet( constrainedContainerElementTypes );
		this.cascaded = cascaded;
		this.groupConversions = CollectionHelper.toImmutableSet( groupConversions );
	}

	@Override
	public Class<?> getContainerClass() {
		return containerClass;
	}

	@Override
	public Integer getTypeArgumentIndex() {
		return typeArgumentIndex;
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
		sb.append( "ContainerElementTypeDescriptorImpl{" );
		sb.append( "containerClass=" ).append( containerClass );
		sb.append( ", typeArgumentIndex=" ).append( typeArgumentIndex );
		sb.append( ", cascaded=" ).append( cascaded );
		sb.append( '}' );
		return sb.toString();
	}
}
