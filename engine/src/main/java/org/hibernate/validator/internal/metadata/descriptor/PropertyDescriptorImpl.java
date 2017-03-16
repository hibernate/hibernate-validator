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

import javax.validation.metadata.GroupConversionDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Describes a validated property.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class PropertyDescriptorImpl extends ElementDescriptorImpl implements PropertyDescriptor {
	private final boolean cascaded;
	private final String property;
	@Immutable
	private final Set<GroupConversionDescriptor> groupConversions;

	public PropertyDescriptorImpl(Type returnType,
								  String propertyName,
								  Set<ConstraintDescriptorImpl<?>> constraints,
								  boolean cascaded,
								  boolean defaultGroupSequenceRedefined,
								  List<Class<?>> defaultGroupSequence, Set<GroupConversionDescriptor> groupConversions) {
		super( returnType, constraints, defaultGroupSequenceRedefined, defaultGroupSequence );

		this.property = propertyName;
		this.cascaded = cascaded;
		this.groupConversions = CollectionHelper.toImmutableSet( groupConversions );
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
	public String getPropertyName() {
		return property;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "PropertyDescriptorImpl" );
		sb.append( "{property=" ).append( property );
		sb.append( ", cascaded='" ).append( cascaded ).append( '\'' );
		sb.append( '}' );
		return sb.toString();
	}
}
