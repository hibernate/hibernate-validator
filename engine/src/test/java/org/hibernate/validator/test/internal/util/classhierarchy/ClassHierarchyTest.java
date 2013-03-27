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

import java.lang.reflect.Method;
import java.util.List;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchy;
import org.hibernate.validator.internal.util.classhierarchy.Filters;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for {@link ClassHierarchy}.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class ClassHierarchyTest {

	@Test
	public void testGetAllMethods() throws Exception {
		List<Method> methods = ClassHierarchy.forType( Fubar.class ).getAllMethods();
		assertTrue( methods.contains( Snafu.class.getMethod( "snafu" ) ) );
		assertTrue( methods.contains( Susfu.class.getMethod( "susfu" ) ) );
	}

	@Test
	public void testFilter() {
		ClassHierarchy<Fubar> hierarchy = ClassHierarchy.forType( Fubar.class );
		List<Class<? super Fubar>> allSuperTypes = hierarchy.filter();
		assertThat( allSuperTypes ).containsOnly( Fubar.class, Snafu.class, Susfu.class, Object.class );

		List<Class<? super Fubar>> superClasses = hierarchy.filter(
				Filters.excludingInterfaces()
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
