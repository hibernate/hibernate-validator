/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.hibernate.validator.internal.engine.valuehandling.OptionalValueUnwrapper}.
 *
 * @author Khalid Alqinyah
 * @author Hardy Ferentschik
 */
public class OptionalValueUnwrapperTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test(enabled = false)
	// TODO implicit unwrap not supported for the time being
	public void testOptionalUnwrappedValueViolations() {
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo() );
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"integerOptional",
				"optionalLong"
		);
		assertCorrectConstraintTypes( constraintViolations, Min.class, Max.class );
	}

	@Test
	public void testOptionalCascadedValidation() {
		Set<ConstraintViolation<Snafu>> constraintViolations = validator.validate( new Snafu() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"barOptional.number"
		);
		assertCorrectConstraintTypes( constraintViolations, Min.class );
	}

	@Test(enabled = false)
	// TODO implicit unwrap not supported for now
	public void testOptionalUnwrappedExecutableReturnValue() throws Exception {
		ExecutableValidator executableValidator = validator.forExecutables();
		Method method = Foo.class.getMethod( "getOptionalLong" );
		Set<ConstraintViolation<Foo>> constraintViolations = executableValidator.validateReturnValue(
				new Foo(),
				method,
				Optional.of( 9L )
		);
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "getOptionalLong.<return value>" );
		assertCorrectConstraintTypes( constraintViolations, Max.class );
	}

	@Test
	public void testOptionalUnwrappedExecutableParameter() throws Exception {
		ExecutableValidator executableValidator = validator.forExecutables();
		Method method = Baz.class.getMethod( "setOptionalLong", Optional.class );
		Object[] values = { Optional.of( 2L ) };
		Set<ConstraintViolation<Baz>> constraintViolations = executableValidator.validateParameters(
				new Baz(),
				method,
				values
		);
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "setOptionalLong.optionalLongPara" );
		assertCorrectConstraintTypes( constraintViolations, Min.class );
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
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "getBar.<return value>.number" );
		assertCorrectConstraintTypes( constraintViolations, Min.class );
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
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "setBar.optionalBarPara.number" );
		assertCorrectConstraintTypes( constraintViolations, Min.class );
	}

	@Test(enabled = false)
	// TODO nested extraction not handled yet
	@TestForIssue(jiraKey = "HV-895")
	public void cascaded_validation_applies_for_elements_of_list_wrapped_in_optional() {
		Set<ConstraintViolation<Quux>> constraintViolations = validator.validate( new Quux() );
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"bar[0].number",
				"bar[1].number"
		);
		assertCorrectConstraintTypes( constraintViolations, Min.class, Min.class );
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
