/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.testutil;

import java.util.Set;
import javax.validation.metadata.GroupConversionDescriptor;

import org.fest.assertions.CollectionAssert;

import static org.fest.assertions.Formatting.format;

/**
 * Provides assertion methods for testing {@link javax.validation.metadata.ElementDescriptor}
 * implementations and collections thereof.
 *
 * @author Gunnar Morling
 */
public class DescriptorAssert {

	public static GroupConversionDescriptorSetAssert assertThat(Set<GroupConversionDescriptor> groupConversions) {
		return new GroupConversionDescriptorSetAssert( groupConversions );
	}

	/**
	 * Assertions for collections of {@link GroupConversionDescriptor}s.
	 *
	 * @author Gunnar Morling
	 */
	public static class GroupConversionDescriptorSetAssert extends CollectionAssert {

		protected GroupConversionDescriptorSetAssert(Set<GroupConversionDescriptor> actual) {
			super( actual );
		}

		public void containsConversion(Class<?> from, Class<?> to) {
			isNotNull();

			boolean foundMatchingConversion = false;

			@SuppressWarnings("unchecked")
			Iterable<GroupConversionDescriptor> actualConversions = (Iterable<GroupConversionDescriptor>) actual;

			for ( GroupConversionDescriptor groupConversionDescriptor : actualConversions ) {
				if ( groupConversionDescriptor.getFrom().equals( from ) &&
						groupConversionDescriptor.getTo().equals( to ) ) {
					foundMatchingConversion = true;
					break;
				}
			}

			if ( !foundMatchingConversion ) {
				fail( format( "<%s> does not contain a conversion from <%s> to <%s>.", actual, from, to ) );
			}
		}
	}
}
