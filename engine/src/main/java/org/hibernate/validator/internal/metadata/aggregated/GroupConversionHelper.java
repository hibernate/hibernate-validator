/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.validation.GroupSequence;
import javax.validation.metadata.GroupConversionDescriptor;

import org.hibernate.validator.internal.metadata.descriptor.GroupConversionDescriptorImpl;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Provides group conversion functionality to {@link org.hibernate.validator.cfg.context.Cascadable}s.
 *
 * @author Gunnar Morling
 */
public class GroupConversionHelper {
	private static final Log log = LoggerFactory.make();
	private final Map<Class<?>, Class<?>> groupConversions;

	public GroupConversionHelper(Map<Class<?>, Class<?>> groupConversions) {
		this.groupConversions = Collections.unmodifiableMap( groupConversions );
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

		return Collections.unmodifiableSet( descriptors );
	}

	public void validateGroupConversions(boolean isCascaded, String location) {
		//group conversions may only be configured for cascadable elements
		if ( !isCascaded && !groupConversions.isEmpty() ) {
			throw log.getGroupConversionOnNonCascadingElementException( location );
		}

		//group conversions may not be configured using a sequence as source
		for ( Class<?> oneGroup : groupConversions.keySet() ) {
			if ( isGroupSequence( oneGroup ) ) {
				throw log.getGroupConversionForSequenceException( oneGroup );
			}
		}
	}

	private boolean isGroupSequence(Class<?> oneGroup) {
		return oneGroup.isAnnotationPresent( GroupSequence.class );
	}
}
