/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
