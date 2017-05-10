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

import javax.validation.metadata.ContainerElementTypeDescriptor;
import javax.validation.metadata.GroupConversionDescriptor;
import javax.validation.metadata.ReturnValueDescriptor;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class ReturnValueDescriptorImpl extends ElementDescriptorImpl
		implements ReturnValueDescriptor {

	@Immutable
	private final List<ContainerElementTypeDescriptor> containerElementTypes;

	private final boolean cascaded;

	@Immutable
	private final Set<GroupConversionDescriptor> groupConversions;

	public ReturnValueDescriptorImpl(Type returnType,
									 Set<ConstraintDescriptorImpl<?>> returnValueConstraints,
									 List<ContainerElementTypeDescriptor> containerElementTypes,
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

		this.containerElementTypes = containerElementTypes;
		this.cascaded = cascaded;
		this.groupConversions = CollectionHelper.toImmutableSet( groupConversions );
	}

	@Override
	public List<ContainerElementTypeDescriptor> getContainerElementTypes() {
		return CollectionHelper.toImmutableList( containerElementTypes );
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
