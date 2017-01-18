/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.UnexpectedTypeException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.Null;
import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.UnwrapByDefault;
import javax.validation.valueextraction.Unwrapping;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the various scenarios for explicit and implicit unwrapping of values.
 *
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
@TestForIssue(jiraKey = "HV-925")
@SuppressWarnings("unused")
public class UnwrappingTest {
	private Validator validatorWithValueExtractor;
	private Validator validatorWithoutValueExtractor;

	@BeforeClass
	public void setupValidator() {
		validatorWithoutValueExtractor = ValidatorUtil.getValidator();

		validatorWithValueExtractor = ValidatorUtil.getConfiguration()
				.addCascadedValueExtractor( new ValueHolderExtractor() )
				.addCascadedValueExtractor( new UnwrapByDefaultWrapperExtractor() )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000030.*")
	public void no_constraint_validator_for_wrapped_value_throws_exception() {
		validatorWithoutValueExtractor.validate( new Foo() );
	}

	@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000030.*")
	public void no_constraint_validator_for_unwrapped_value_throws_exception() {
		validatorWithValueExtractor.validate( new Fubar() );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000198.*")
	public void missing_value_extractor_throws_exception() {
		validatorWithoutValueExtractor.validate( new Foobar() );
	}

	@Test
	public void validate_wrapper_itself_if_there_is_no_value_extractor() {
		Set<ConstraintViolation<Qux>> constraintViolations = validatorWithoutValueExtractor.validate( new Qux() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "integerHolder" );
		assertCorrectConstraintTypes( constraintViolations, ValueHolderConstraint.class );
	}

	@Test
	public void validate_wrapper_itself_even_if_there_is_a_value_extractor() {
		Set<ConstraintViolation<Qux>> constraintViolations = validatorWithValueExtractor.validate( new Qux() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "integerHolder" );
		assertCorrectConstraintTypes( constraintViolations, ValueHolderConstraint.class );

		// execute validation twice to ensure that the handling for this case is not subjective to caching (see HV-976)
		constraintViolations = validatorWithValueExtractor.validate( new Qux() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "integerHolder" );
		assertCorrectConstraintTypes( constraintViolations, ValueHolderConstraint.class );
	}

	@Test
	public void validate_wrapper_itself_if_there_is_no_value_extractor_even_if_constraint_could_be_applied_to_unwrapped_value() {
		Set<ConstraintViolation<Baz>> constraintViolations = validatorWithoutValueExtractor.validate( new Baz() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "integerHolder" );
		assertCorrectConstraintTypes( constraintViolations, Null.class );
	}

	@Test(enabled = false, expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000186.*")
	// TODO implicit unwrapping not supported for now
	public void constraint_declaration_exception_if_there_are_validators_for_wrapper_and_wrapped_value() {
		validatorWithValueExtractor.validate( new Baz() );
	}

	@Test
	public void validate_wrapped_value_if_value_extractor_unwraps_by_default() {
		Set<ConstraintViolation<WrapperWithImplicitUnwrapping>> constraintViolations = validatorWithValueExtractor.validate( new WrapperWithImplicitUnwrapping() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "integerWrapper" );
		assertCorrectConstraintTypes( constraintViolations, Min.class );
	}

	@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000030.*")
	public void validation_exception_if_unwrapping_disabled_per_constraint() {
		validatorWithValueExtractor.validate( new WrapperWithDisabledUnwrapping() );
	}

	@Test
	public void validate_wrapped_value_if_value_extractor_unwraps_by_default_and_unwrapping_enabled_per_constraint() {
		Set<ConstraintViolation<WrapperWithForcedUnwrapping>> constraintViolations = validatorWithValueExtractor.validate( new WrapperWithForcedUnwrapping() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "integerWrapper" );
		assertCorrectConstraintTypes( constraintViolations, Min.class );
	}

	public class Foo {
		// no constraint validator defined for @DummyConstraint
		@DummyConstraint
		private final ValueHolder<Integer> integerHolder = new ValueHolder<>( 5 );
	}

	public class Fubar {
		// no constraint validator for the wrapped value
		@Future(payload = { Unwrapping.Unwrap.class })
		private final ValueHolder<Integer> integerHolder = new ValueHolder<>( 5 );
	}

	public class Foobar {
		@Min(value = 10, payload = { Unwrapping.Unwrap.class })
		private final ValueHolder<Integer> integerHolder = new ValueHolder<>( 5 );
	}

	public class Bar {
		@Min(10)
		private final ValueHolder<Integer> integerHolder = new ValueHolder<>( 5 );

		private final ValueHolder<@NotBlank String> stringHolder = new ValueHolder<>( "" );
	}

	public class Baz {
		@Null
		private final ValueHolder<Integer> integerHolder = new ValueHolder<>( 5 );
	}

	public class Qux {
		@ValueHolderConstraint
		private final ValueHolder<Integer> integerHolder = new ValueHolder<>( 5 );
	}

	public class WrapperWithImplicitUnwrapping {

		@Min(10)
		private final Wrapper<Integer> integerWrapper = new Wrapper<Integer>( 5 );
	}

	public class WrapperWithDisabledUnwrapping {

		@Min(value = 10, payload = { Unwrapping.Skip.class })
		private final Wrapper<Integer> integerWrapper = new Wrapper<Integer>( 5 );
	}

	public class WrapperWithForcedUnwrapping {

		@Min(value = 10, payload = { Unwrapping.Unwrap.class })
		private final Wrapper<Integer> integerWrapper = new Wrapper<Integer>( 5 );
	}

	class ValueHolder<T> {

		ValueHolder(T value) {
			this.value = value;
		}

		private final T value;

		public T getValue() {
			return value;
		}
	}

	class Wrapper<T> {

		Wrapper(T value) {
			this.value = value;
		}

		private final T value;

		public T getValue() {
			return value;
		}
	}

	@Documented
	@Constraint(validatedBy = {})
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	public @interface DummyConstraint {
		String message() default "dummy constraint";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};
	}

	@Documented
	@Constraint(validatedBy = { ValueHandlerConstraintValidator.class })
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	public @interface ValueHolderConstraint {
		String message() default "value holder constraint";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};
	}

	public static class ValueHandlerConstraintValidator
			implements ConstraintValidator<ValueHolderConstraint, ValueHolder> {

		@Override
		public boolean isValid(ValueHolder value, ConstraintValidatorContext context) {
			return false;
		}
	}

	class ValueHolderExtractor implements ValueExtractor<ValueHolder<@ExtractedValue ?>> {

		@Override
		public void extractValues(ValueHolder<@ExtractedValue ?> originalValue, ValueExtractor.ValueReceiver receiver) {
			receiver.value( null, originalValue.value );
		}
	}

	@UnwrapByDefault
	class UnwrapByDefaultWrapperExtractor implements ValueExtractor<Wrapper<@ExtractedValue ?>> {

		@Override
		public void extractValues(Wrapper<@ExtractedValue ?> originalValue, ValueExtractor.ValueReceiver receiver) {
			receiver.value( null, originalValue.value );
		}
	}

}
