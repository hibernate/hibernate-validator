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
import jakarta.validation.metadata.ParameterDescriptor;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Describes a validated method parameter.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class ParameterDescriptorImpl extends ElementDescriptorImpl implements ParameterDescriptor {

	private final int index;

	private final String name;

	@Immutable
	private final Set<ContainerElementTypeDescriptor> constrainedContainerElementTypes;

	private final boolean cascaded;

	@Immutable
	private final Set<GroupConversionDescriptor> groupConversions;

	public ParameterDescriptorImpl(Type type,
								   int index,
								   String name,
								   Set<ConstraintDescriptorImpl<?>> constraints,
								   Set<ContainerElementTypeDescriptor> constrainedContainerElementTypes,
								   boolean isCascaded,
								   boolean defaultGroupSequenceRedefined,
								   List<Class<?>> defaultGroupSequence,
								   Set<GroupConversionDescriptor> groupConversions) {
		super( type, constraints, defaultGroupSequenceRedefined, defaultGroupSequence );
		this.index = index;
		this.name = name;
		this.constrainedContainerElementTypes = CollectionHelper.toImmutableSet( constrainedContainerElementTypes );
		this.cascaded = isCascaded;
		this.groupConversions = CollectionHelper.toImmutableSet( groupConversions );
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public Set<ContainerElementTypeDescriptor> getConstrainedContainerElementTypes() {
		return constrainedContainerElementTypes;
	}

	@Override
	public String getName() {
		return name;
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
		sb.append( "ParameterDescriptorImpl" );
		sb.append( "{cascaded=" ).append( cascaded );
		sb.append( ", index=" ).append( index );
		sb.append( ", name=" ).append( name );
		sb.append( '}' );
		return sb.toString();
	}
}
