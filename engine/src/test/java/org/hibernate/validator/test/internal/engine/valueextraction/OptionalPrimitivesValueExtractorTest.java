/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;

import org.hibernate.validator.testutils.CandidateForTck;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Guillaume Smet
 */
@CandidateForTck
public class OptionalPrimitivesValueExtractorTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void testOptionalInt() {
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( Foo.valid() );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( Foo.empty() );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( Foo.invalid() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class ).withProperty( "optionalInt" ),
				violationOf( DecimalMin.class ).withProperty( "optionalDouble" ),
				violationOf( Min.class ).withProperty( "optionalLong" )
		);
	}

	private static class Foo {

		@Min(value = 5)
		private final OptionalInt optionalInt;

		@DecimalMin(value = "5")
		private final OptionalDouble optionalDouble;

		@Min(value = 16)
		private final OptionalLong optionalLong;

		private Foo(OptionalInt optionalInt, OptionalDouble optionalDouble, OptionalLong optionalLong) {
			this.optionalInt = optionalInt;
			this.optionalDouble = optionalDouble;
			this.optionalLong = optionalLong;
		}

		private static Foo valid() {
			return new Foo(
					OptionalInt.of( 7 ),
					OptionalDouble.of( 8D ),
					OptionalLong.of( 19L )
			);
		}

		private static Foo invalid() {
			return new Foo(
					OptionalInt.of( 3 ),
					OptionalDouble.of( 2D ),
					OptionalLong.of( 10L )
			);
		}

		private static Foo empty() {
			return new Foo(
					OptionalInt.empty(),
					OptionalDouble.empty(),
					OptionalLong.empty()
			);
		}
	}
}
