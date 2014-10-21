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
import javax.validation.metadata.GroupConversionDescriptor;
import javax.validation.metadata.ParameterDescriptor;

/**
 * Describes a validated method parameter.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class ParameterDescriptorImpl extends ElementDescriptorImpl implements ParameterDescriptor {
	private final int index;
	private final String name;
	private final boolean cascaded;
	private final Set<GroupConversionDescriptor> groupConversions;

	public ParameterDescriptorImpl(Type type,
								   int index,
								   String name,
								   Set<ConstraintDescriptorImpl<?>> constraints,
								   boolean isCascaded,
								   boolean defaultGroupSequenceRedefined,
								   List<Class<?>> defaultGroupSequence,
								   Set<GroupConversionDescriptor> groupConversions) {
		super( type, constraints, defaultGroupSequenceRedefined, defaultGroupSequence );
		this.index = index;
		this.name = name;
		this.cascaded = isCascaded;
		this.groupConversions = Collections.unmodifiableSet( groupConversions );
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
	public int getIndex() {
		return index;
	}

	@Override
	public String getName() {
		return name;
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
