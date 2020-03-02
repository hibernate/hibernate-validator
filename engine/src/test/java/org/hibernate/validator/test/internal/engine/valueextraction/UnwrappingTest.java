/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.UnexpectedTypeException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.UnwrapByDefault;
import jakarta.validation.valueextraction.Unwrapping;
import jakarta.validation.valueextraction.ValueExtractor;

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
public class UnwrappingTest {
	private Validator validatorWithValueExtractor;
	private Validator validatorWithoutValueExtractor;

	@BeforeClass
	public void setupValidator() {
		validatorWithoutValueExtractor = ValidatorUtil.getValidator();

		validatorWithValueExtractor = ValidatorUtil.getConfiguration()
				.addValueExtractor( new ValueHolderExtractor() )
				.addValueExtractor( new UnwrapByDefaultWrapperValueExtractor() )
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

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000198.*")
	public void missing_value_extractor_throws_exception() {
		validatorWithoutValueExtractor.validate( new Foobar() );
	}

	@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000030.*")
	public void validation_exception_if_unwrapping_disabled_per_constraint() {
		validatorWithValueExtractor.validate( new WrapperWithDisabledUnwrapping() );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000223.*")
	public void validate_wrapped_value_while_wrapper_has_two_type_parameters_but_two_value_extractors_raises_exception() {
		Validator validator = ValidatorUtil.getConfiguration()
				.addValueExtractor( new WrapperWithTwoTypeArgumentsFirstValueExtractor() )
				.addValueExtractor( new WrapperWithTwoTypeArgumentsSecondValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		validator.validate( new BeanWithWrapperWithTwoTypeArguments() );
	}

	private class Foo {
		// no constraint validator defined for @DummyConstraint
		@DummyConstraint
		private final ValueHolder<Integer> integerHolder = new ValueHolder<>( 5 );
	}

	private class Fubar {
		// no constraint validator for the wrapped value
		@Future(payload = { Unwrapping.Unwrap.class })
		private final ValueHolder<Integer> integerHolder = new ValueHolder<>( 5 );
	}

	private class Foobar {
		@Min(value = 10, payload = { Unwrapping.Unwrap.class })
		private final ValueHolder<Integer> integerHolder = new ValueHolder<>( 5 );
	}

	private class WrapperWithDisabledUnwrapping {

		@Min(value = 10, payload = { Unwrapping.Skip.class })
		private final Wrapper<Integer> integerWrapper = new Wrapper<>( 5 );
	}

	private class BeanWithWrapperWithTwoTypeArguments {

		@Min(value = 10)
		private final WrapperWithTwoTypeArguments<Long, String> wrapper = new WrapperWithTwoTypeArguments<>( 5L, "value" );
	}

	private class ValueHolder<T> {

		private final T value;

		private ValueHolder(T value) {
			this.value = value;
		}

		@SuppressWarnings("unused")
		public T getValue() {
			return value;
		}
	}

	private class Wrapper<T> {

		private final T value;

		private Wrapper(T value) {
			this.value = value;
		}

		@SuppressWarnings("unused")
		public T getValue() {
			return value;
		}
	}

	private class WrapperWithTwoTypeArguments<T, U> {

		private final T value1;
		private final U value2;

		private WrapperWithTwoTypeArguments(T value1, U value2) {
			this.value1 = value1;
			this.value2 = value2;
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
			implements ConstraintValidator<ValueHolderConstraint, ValueHolder<?>> {

		@Override
		public boolean isValid(ValueHolder<?> value, ConstraintValidatorContext context) {
			return false;
		}
	}

	private class ValueHolderExtractor implements ValueExtractor<ValueHolder<@ExtractedValue ?>> {

		@Override
		public void extractValues(ValueHolder<@ExtractedValue ?> originalValue, ValueExtractor.ValueReceiver receiver) {
			receiver.value( null, originalValue.value );
		}
	}

	@UnwrapByDefault
	private class UnwrapByDefaultWrapperValueExtractor implements ValueExtractor<Wrapper<@ExtractedValue ?>> {

		@Override
		public void extractValues(Wrapper<@ExtractedValue ?> originalValue, ValueExtractor.ValueReceiver receiver) {
			receiver.value( null, originalValue.value );
		}
	}

	@UnwrapByDefault
	private class WrapperWithTwoTypeArgumentsFirstValueExtractor implements ValueExtractor<WrapperWithTwoTypeArguments<@ExtractedValue ?, ?>> {

		@Override
		public void extractValues(WrapperWithTwoTypeArguments<?, ?> originalValue, ValueExtractor.ValueReceiver receiver) {
			receiver.value( null, originalValue.value1 );
		}
	}

	@UnwrapByDefault
	private class WrapperWithTwoTypeArgumentsSecondValueExtractor implements ValueExtractor<WrapperWithTwoTypeArguments<?, @ExtractedValue ?>> {

		@Override
		public void extractValues(WrapperWithTwoTypeArguments<?, ?> originalValue, ValueExtractor.ValueReceiver receiver) {
			receiver.value( null, originalValue.value2 );
		}
	}
}
