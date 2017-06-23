/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.executable.ExecutableValidator;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.CandidateForTck;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.hibernate.validator.internal.engine.valuehandling.OptionalValueUnwrapper}.
 *
 * @author Khalid Alqinyah
 * @author Hardy Ferentschik
 */
@CandidateForTck
public class OptionalValueExtractorTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void testOptionalCascadedValidation() {
		Set<ConstraintViolation<Snafu>> constraintViolations = validator.validate( new Snafu() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
						.withPropertyPath( pathWith()
								.property( "barOptional" )
								.property( "number", false, null, null, Optional.class, 0 )
						)
		);
	}

	@Test
	public void testOptionalUnwrappedCascadableExecutableReturnValue() throws Exception {
		ExecutableValidator executableValidator = validator.forExecutables();
		Method method = Qux.class.getMethod( "getBar" );
		Set<ConstraintViolation<Qux>> constraintViolations = executableValidator.validateReturnValue(
				new Qux(),
				method,
				Optional.of( new Bar() )
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
						.withPropertyPath( pathWith()
								.method( "getBar" )
								.returnValue()
								.property( "number", false, null, null, Optional.class, 0 )
						)
		);
	}

	@Test
	public void testOptionalUnwrappedCascadableExecutableParameter() throws Exception {
		ExecutableValidator executableValidator = validator.forExecutables();
		Method method = Fubar.class.getMethod( "setBar", Optional.class );
		Object[] values = { Optional.of( new Bar() ) };
		Set<ConstraintViolation<Fubar>> constraintViolations = executableValidator.validateParameters(
				new Fubar(),
				method,
				values
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
						.withPropertyPath( pathWith()
								.method( "setBar" )
								.parameter( "optionalBarPara", 0 )
								.property( "number", false, null, null, Optional.class, 0 )
						)
		);
	}

	@Test(enabled = false)
	// TODO nested extraction not handled yet
	@TestForIssue(jiraKey = "HV-895")
	public void cascaded_validation_applies_for_elements_of_list_wrapped_in_optional() {
		Set<ConstraintViolation<Quux>> constraintViolations = validator.validate( new Quux() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
						.withPropertyPath( pathWith()
								.property( "bar" )
								.property( "number", true, null, 0, Optional.class, 0 )
						),
				violationOf( Min.class )
						.withPropertyPath( pathWith()
								.property( "bar" )
								.property( "number", true, null, 1, Optional.class, 0 )
						)
		);
	}

	private class Foo {
		@Min(value = 5)
		Optional<Integer> integerOptional = Optional.of( 3 );

		@Max(value = 5)
		public Optional<Long> getOptionalLong() {
			return Optional.of( 7L );
		}
	}

	private class Snafu {
		@Valid
		Optional<Bar> barOptional = Optional.of( new Bar() );
	}

	@SuppressWarnings("unused")
	private class Fubar {
		public void setBar(@Valid Optional<Bar> optionalBarPara) {
		}
	}

	private class Qux {
		@Valid
		public Optional<Bar> getBar() {
			return Optional.empty();
		}
	}

	@SuppressWarnings("unused")
	private class Baz {
		public void setOptionalLong(@Min(5) Optional<Long> optionalLongPara) {
		}
	}

	private class Bar {
		@Min(value = 5)
		int number = 3;
	}

	private class Quux {
		@Valid
		public Optional<List<Bar>> getBar() {
			return Optional.of( Arrays.asList( new Bar(), new Bar() ) );
		}
	}
}
