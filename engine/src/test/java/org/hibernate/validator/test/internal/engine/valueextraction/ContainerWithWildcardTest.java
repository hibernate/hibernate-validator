/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
@TestForIssue(jiraKey = "HV-1720")
public class ContainerWithWildcardTest {

	@Test
	public void containerWithUpperBoundWildcard() {
		Set<ConstraintViolation<Foo>> constraintViolations = getValidator().validate( new Foo( Arrays.asList( null, null ) ) );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class ),
				violationOf( NotNull.class )
		);
	}

	@Test
	public void containerWithWildcard() {
		Set<ConstraintViolation<Bar>> constraintViolations = getValidator().validate( new Bar( Arrays.asList( null, null ) ) );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class ),
				violationOf( NotNull.class )
		);
	}

	@Test
	public void containerWithLowerBoundWildcard() {
		Set<ConstraintViolation<FooBar>> constraintViolations = getValidator().validate( new FooBar( Arrays.asList( null, null ) ) );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class ),
				violationOf( NotNull.class )
		);
	}

	private static class Foo {
		private final List<@NotNull ? extends BigDecimal> list;

		private Foo(List<? extends BigDecimal> list) {
			this.list = list;
		}
	}

	private static class Bar {
		private final List<@NotNull ?> list;

		private Bar(List<?> list) {
			this.list = list;
		}
	}

	private static class FooBar {
		private final List<@NotNull ? super BigDecimal> list;

		private FooBar(List<? super BigDecimal> list) {
			this.list = list;
		}
	}
}
