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
import javax.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Describes a validated property.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class PropertyDescriptorImpl extends ElementDescriptorImpl implements PropertyDescriptor {

	private final String propertyName;

	@Immutable
	private final List<ContainerElementTypeDescriptor> containerElementTypes;

	private final boolean cascaded;

	@Immutable
	private final Set<GroupConversionDescriptor> groupConversions;

	public PropertyDescriptorImpl(Type returnType,
								  String propertyName,
								  Set<ConstraintDescriptorImpl<?>> constraints,
								  List<ContainerElementTypeDescriptor> containerElementTypes,
								  boolean cascaded,
								  boolean defaultGroupSequenceRedefined,
								  List<Class<?>> defaultGroupSequence,
								  Set<GroupConversionDescriptor> groupConversions) {
		super( returnType, constraints, defaultGroupSequenceRedefined, defaultGroupSequence );

		this.propertyName = propertyName;
		this.containerElementTypes = containerElementTypes;
		this.cascaded = cascaded;
		this.groupConversions = CollectionHelper.toImmutableSet( groupConversions );
	}

	@Override
	public String getPropertyName() {
		return propertyName;
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
		sb.append( "PropertyDescriptorImpl" );
		sb.append( "{propertyName=" ).append( propertyName );
		sb.append( ", cascaded=" ).append( cascaded );
		sb.append( '}' );
		return sb.toString();
	}
}
