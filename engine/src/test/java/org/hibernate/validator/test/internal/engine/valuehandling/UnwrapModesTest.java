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
import java.lang.reflect.Type;
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

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.spi.cascading.ExtractedValue;
import org.hibernate.validator.spi.cascading.ValueExtractor;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;

/**
 * Test the various scenarios for explicit and implicit unwrapping of values.
 *
 * @author Hardy Ferentschik
 */
@TestForIssue(jiraKey = "HV-925")
@SuppressWarnings("unused")
public class UnwrapModesTest {
	private Validator validatorWithUnwrapper;
	private Validator validatorWithoutUnwrapper;

	@BeforeClass
	public void setupValidator() {
		validatorWithoutUnwrapper = ValidatorUtil.getValidator();

		validatorWithUnwrapper = ValidatorUtil.getConfiguration()
				.addValidatedValueHandler( new ValueHolderUnwrapper() )
				.addCascadedValueExtractor( new ValueHolderExtractor() )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000030.*")
	public void no_constraint_validator_for_wrapped_and_unwrapped_value_throws_exception() {
		validatorWithoutUnwrapper.validate( new Foo() );
	}

	@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000030.*")
	public void no_constraint_validator_for_unwrapped_value_throws_exception() {
		validatorWithUnwrapper.validate( new Fubar() );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000182.*")
	public void missing_value_unwrapper_throws_exception() {
		validatorWithoutUnwrapper.validate( new Foobar() );
	}

	@Test
	public void unwrapped_values_get_validated() {
		Set<ConstraintViolation<Bar>> constraintViolations = validatorWithUnwrapper.validate( new Bar() );
		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectPropertyPaths( constraintViolations, "integerHolder", "stringHolder" );
		assertCorrectConstraintTypes( constraintViolations, Min.class, NotBlank.class );
	}

	@Test
	public void validate_wrapper_itself_if_there_is_no_unwrapper_and_no_validator_for_wrapped_value() {
		Set<ConstraintViolation<Qux>> constraintViolations = validatorWithoutUnwrapper.validate( new Qux() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "integerHolder" );
		assertCorrectConstraintTypes( constraintViolations, ValueHolderConstraint.class );
	}

	@Test
	public void validate_wrapper_itself_if_there_is_unwrapper_but_only_constraint_validator_for_wrapper() {
		Set<ConstraintViolation<Qux>> constraintViolations = validatorWithUnwrapper.validate( new Qux() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "integerHolder" );
		assertCorrectConstraintTypes( constraintViolations, ValueHolderConstraint.class );

		// execute validation twice to ensure that the handling for this case is not subjective to caching (see HV-976)
		constraintViolations = validatorWithUnwrapper.validate( new Qux() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "integerHolder" );
		assertCorrectConstraintTypes( constraintViolations, ValueHolderConstraint.class );
	}

	@Test
	public void validate_wrapper_itself_if_there_is_no_unwrapper() {
		Set<ConstraintViolation<Baz>> constraintViolations = validatorWithoutUnwrapper.validate( new Baz() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "integerHolder" );
		assertCorrectConstraintTypes( constraintViolations, Null.class );
	}

	@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000186.*")
	public void constraint_declaration_exception_if_there_are_validators_for_wrapper_and_wrapped_value() {
		validatorWithUnwrapper.validate( new Baz() );
	}

	public class Foo {
		// no constraint validator defined for @DummyConstraint and no unwrapper for ValueHolder
		@DummyConstraint
		private final ValueHolder<Integer> integerHolder = new ValueHolder<>( 5 );
	}

	public class Fubar {
		// @UnwrapValidatedValue annotation optional
		@Future // there is no future validator for integers!
		private final ValueHolder<Integer> integerHolder = new ValueHolder<>( 5 );
	}

	public class Foobar {
		@UnwrapValidatedValue
		@Min(10)
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

	class ValueHolder<T> {

		ValueHolder(T value) {
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
		public void initialize(ValueHolderConstraint constraintAnnotation) {

		}

		@Override
		public boolean isValid(ValueHolder value, ConstraintValidatorContext context) {
			return false;
		}
	}

	class ValueHolderUnwrapper extends ValidatedValueUnwrapper<ValueHolder> {
		TypeResolver typeResolver = new TypeResolutionHelper().getTypeResolver();

		@Override
		public Object handleValidatedValue(ValueHolder valueHolder) {
			return valueHolder.getValue();
		}

		@Override
		public Type getValidatedValueType(Type valueType) {
			ResolvedType resolvedType = typeResolver.resolve( valueType );
			return resolvedType.typeParametersFor( ValueHolder.class ).get( 0 ).getErasedType();
		}
	}

	class ValueHolderExtractor implements ValueExtractor<ValueHolder<@ExtractedValue ?>> {

		@Override
		public void extractValues(ValueHolder<@ExtractedValue ?> originalValue, ValueExtractor.ValueReceiver receiver) {
			receiver.value( originalValue.value, null );
		}
	}

}
