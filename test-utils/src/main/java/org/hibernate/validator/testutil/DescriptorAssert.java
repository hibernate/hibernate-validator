/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.testutil;

import static org.testng.Assert.fail;

import java.util.Locale;
import java.util.Set;

import jakarta.validation.metadata.GroupConversionDescriptor;

import org.assertj.core.api.IterableAssert;

/**
 * Provides assertion methods for testing {@link jakarta.validation.metadata.ElementDescriptor}
 * implementations and collections thereof.
 *
 * @author Gunnar Morling
 */
public class DescriptorAssert {

	private DescriptorAssert() {
		// Not allowed
	}

	public static GroupConversionDescriptorSetAssert assertThat(Set<GroupConversionDescriptor> groupConversions) {
		return new GroupConversionDescriptorSetAssert( groupConversions );
	}

	/**
	 * Assertions for collections of {@link GroupConversionDescriptor}s.
	 *
	 * @author Gunnar Morling
	 */
	public static class GroupConversionDescriptorSetAssert extends IterableAssert<GroupConversionDescriptor> {

		protected GroupConversionDescriptorSetAssert(Set<GroupConversionDescriptor> actual) {
			super( actual );
		}

		public void containsConversion(Class<?> from, Class<?> to) {
			isNotNull();

			boolean foundMatchingConversion = false;

			for ( GroupConversionDescriptor groupConversionDescriptor : actual ) {
				if ( groupConversionDescriptor.getFrom().equals( from ) &&
						groupConversionDescriptor.getTo().equals( to ) ) {
					foundMatchingConversion = true;
					break;
				}
			}

			if ( !foundMatchingConversion ) {
				fail( String.format( Locale.ROOT, "<%s> does not contain a conversion from <%s> to <%s>.", actual, from, to ) );
			}
		}
	}
}
