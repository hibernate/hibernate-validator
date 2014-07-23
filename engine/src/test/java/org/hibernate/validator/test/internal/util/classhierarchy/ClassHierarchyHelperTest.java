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
package org.hibernate.validator.test.internal.util.classhierarchy;

import java.util.List;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;
import org.hibernate.validator.internal.util.classhierarchy.Filters;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Unit test for {@link ClassHierarchyHelper}.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class ClassHierarchyHelperTest {

	@Test
	public void testGetHierarchy() {
		List<Class<? super Fubar>> hierarchy = ClassHierarchyHelper.getHierarchy( Fubar.class );
		assertThat( hierarchy ).containsOnly( Fubar.class, Snafu.class, Susfu.class, Object.class );

		List<Class<? super Fubar>> superClasses = ClassHierarchyHelper.getHierarchy(
				Fubar.class,
				Filters.excludeInterfaces()
		);
		assertThat( superClasses ).containsOnly( Fubar.class, Object.class );
	}

	private interface Snafu {
		void snafu();
	}

	private interface Susfu {
		void susfu();
	}

	private static class Fubar implements Snafu, Susfu {

		@Override
		public void snafu() {
		}

		@Override
		public void susfu() {
		}
	}
}
