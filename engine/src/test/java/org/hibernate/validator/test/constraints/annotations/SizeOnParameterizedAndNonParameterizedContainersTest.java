/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.annotations;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
@TestForIssue(jiraKey = "HV-1551")
@SuppressWarnings("rawtypes")
public class SizeOnParameterizedAndNonParameterizedContainersTest extends AbstractConstrainedTest {

	@Test
	public void testNonParameterizedMap() throws Exception {
		class Foo {

			@Size(min = 1)
			private Map prop;

			public Foo(Map prop) {
				this.prop = prop;
			}
		}

		Foo foo = new Foo( new HashMap() );

		assertSizeViolations( validator.validate( foo ) );
	}

	@Test
	public void testParameterizedMap() throws Exception {
		class Foo {

			@Size(min = 1)
			private Map<String, Integer> prop;

			public Foo(Map<String, Integer> prop) {
				this.prop = prop;
			}
		}

		Foo foo = new Foo( new HashMap<>() );

		assertSizeViolations( validator.validate( foo ) );
	}

	@Test
	public void testNonParameterizedCollection() throws Exception {
		class Foo {

			@Size(min = 1)
			private Collection prop;

			public Foo(Collection prop) {
				this.prop = prop;
			}
		}

		Foo foo = new Foo( new ArrayList() );

		assertSizeViolations( validator.validate( foo ) );
	}

	@Test
	public void testParameterizedCollection() throws Exception {
		class Foo {

			@Size(min = 1)
			private Collection<String> prop;

			public Foo(Collection<String> prop) {
				this.prop = prop;
			}
		}

		Foo foo = new Foo( new ArrayList<>() );

		assertSizeViolations( validator.validate( foo ) );
	}

	@Test
	public void testNonParameterizedSet() throws Exception {
		class Foo {

			@Size(min = 1)
			private Set prop;

			public Foo(Set prop) {
				this.prop = prop;
			}
		}

		Foo foo = new Foo( new HashSet() );

		assertSizeViolations( validator.validate( foo ) );
	}

	@Test
	public void testParameterizedSet() throws Exception {
		class Foo {

			@Size(min = 1)
			private Set<String> prop;

			public Foo(Set<String> prop) {
				this.prop = prop;
			}
		}

		Foo foo = new Foo( new HashSet<>() );

		assertSizeViolations( validator.validate( foo ) );
	}

	private <T> void assertSizeViolations(Set<ConstraintViolation<T>> violations) {
		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class )
		);
	}
}
