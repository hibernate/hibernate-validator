/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.util.classhierarchy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;
import org.hibernate.validator.internal.util.classhierarchy.Filters;

import org.testng.annotations.Test;

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
				Filters.excludeInterfaces( Fubar.class )
		);
		assertThat( superClasses ).containsOnly( Fubar.class, Object.class );
	}

	@Test
	public void testHierarchyWithoutInterfaces() {
		List<Class<? super Snafu>> superClasses = ClassHierarchyHelper.getHierarchy(
				Snafu.class
		);
		assertThat( superClasses ).containsOnly( Snafu.class );

		superClasses = ClassHierarchyHelper.getHierarchy(
				Snafu.class,
				Filters.excludeInterfaces( Snafu.class )
		);
		assertThat( superClasses ).containsOnly( Snafu.class );
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
