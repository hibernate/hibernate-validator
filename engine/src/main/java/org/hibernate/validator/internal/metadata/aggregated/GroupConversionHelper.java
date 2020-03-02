/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.validation.metadata.GroupConversionDescriptor;

import org.hibernate.validator.internal.metadata.descriptor.GroupConversionDescriptorImpl;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Provides group conversion functionality to {@link org.hibernate.validator.cfg.context.Cascadable}s.
 *
 * @author Gunnar Morling
 */
public class GroupConversionHelper {

	static final GroupConversionHelper EMPTY = new GroupConversionHelper( Collections.emptyMap() );

	@Immutable
	private final Map<Class<?>, Class<?>> groupConversions;

	private GroupConversionHelper(Map<Class<?>, Class<?>> groupConversions) {
		this.groupConversions = CollectionHelper.toImmutableMap( groupConversions );
	}

	public static GroupConversionHelper of(Map<Class<?>, Class<?>> groupConversions) {
		if ( groupConversions.isEmpty() ) {
			return GroupConversionHelper.EMPTY;
		}
		else {
			return new GroupConversionHelper( groupConversions );
		}
	}

	/**
	 * Converts the given validation group as per the group conversion
	 * configuration for this property (as e.g. specified via
	 * {@code @ConvertGroup}.
	 *
	 * @param from The group to convert.
	 *
	 * @return The converted group. Will be the original group itself in case no
	 *         conversion is to be performed.
	 */
	public Class<?> convertGroup(Class<?> from) {
		Class<?> to = groupConversions.get( from );
		return to != null ? to : from;
	}

	/**
	 * Returns a set with {@link GroupConversionDescriptor}s representing the
	 * underlying group conversions.
	 *
	 * @return A set with group conversion descriptors. May be empty, but never
	 *         {@code null}.
	 */
	public Set<GroupConversionDescriptor> asDescriptors() {
		Set<GroupConversionDescriptor> descriptors = newHashSet( groupConversions.size() );

		for ( Entry<Class<?>, Class<?>> conversion : groupConversions.entrySet() ) {
			descriptors.add(
					new GroupConversionDescriptorImpl(
							conversion.getKey(),
							conversion.getValue()
					)
			);
		}

		return CollectionHelper.toImmutableSet( descriptors );
	}

	boolean isEmpty() {
		return groupConversions.isEmpty();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( getClass().getSimpleName() );
		sb.append( " [" );
		sb.append( "groupConversions=" ).append( groupConversions );
		sb.append( "]" );
		return sb.toString();
	}
}
