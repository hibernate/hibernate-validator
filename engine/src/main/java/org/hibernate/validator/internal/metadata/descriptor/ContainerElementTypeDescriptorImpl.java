package org.hibernate.validator.internal.metadata.descriptor;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.validation.metadata.ContainerElementTypeDescriptor;
import javax.validation.metadata.GroupConversionDescriptor;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

public class ContainerElementTypeDescriptorImpl extends ElementDescriptorImpl implements ContainerElementTypeDescriptor {

	private final Integer typeArgumentIndex;

	@Immutable
	private final List<ContainerElementTypeDescriptor> containerElementTypes;

	private final boolean cascaded;

	@Immutable
	private final Set<GroupConversionDescriptor> groupConversions;

	public ContainerElementTypeDescriptorImpl(Type type,
			Integer typeArgumentIndex,
			Set<ConstraintDescriptorImpl<?>> constraints,
			List<ContainerElementTypeDescriptor> containerElementTypes,
			boolean cascaded,
			boolean defaultGroupSequenceRedefined,
			List<Class<?>> defaultGroupSequence,
			Set<GroupConversionDescriptor> groupConversions) {
		super( type, constraints, defaultGroupSequenceRedefined, defaultGroupSequence );

		this.typeArgumentIndex = typeArgumentIndex;
		this.containerElementTypes = containerElementTypes;
		this.cascaded = cascaded;
		this.groupConversions = CollectionHelper.toImmutableSet( groupConversions );
	}

	@Override
	public Integer getTypeArgumentIndex() {
		return typeArgumentIndex;
	}

	@Override
	public List<ContainerElementTypeDescriptor> getContainerElementTypes() {
		return containerElementTypes;
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
		sb.append( "typeArgumentIndex=" ).append( typeArgumentIndex );
		sb.append( ", cascaded=" ).append( cascaded );
		sb.append( '}' );
		return sb.toString();
	}
}
