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
import javax.validation.metadata.ReturnValueDescriptor;

/**
 * @author Hardy Ferentschik
 */
public class ReturnValueDescriptorImpl extends ElementDescriptorImpl
		implements ReturnValueDescriptor {
	private final boolean cascaded;
	private final Set<GroupConversionDescriptor> groupConversions;

	public ReturnValueDescriptorImpl(Type returnType,
									 Set<ConstraintDescriptorImpl<?>> returnValueConstraints,
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

		this.cascaded = cascaded;
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
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ReturnValueDescriptorImpl" );
		sb.append( "{cascaded=" ).append( cascaded );
		sb.append( '}' );
		return sb.toString();
	}
}
