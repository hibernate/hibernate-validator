/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.cascaded;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class CascadingOnClassLevelConstraintTest {

	@Test
	@TestForIssue(jiraKey = "HV-509")
	public void testCascadedValidation() {
		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<Bar>> violations = validator.validate( new Bar() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( ValidFoo.class )
						.withPropertyPath( pathWith()
								.property( "foos" )
								.bean( true, null, 0, List.class, 0 )

						),
				violationOf( ValidFoo.class )
						.withPropertyPath( pathWith()
								.property( "foos" )
								.bean( true, null, 1, List.class, 0 )

						)
		);
	}

	@Test
	public void testCascadedValidationViaTypeParameterOnField() {
		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<BarUsingTypeParameterOnField>> violations = validator.validate( new BarUsingTypeParameterOnField() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( ValidFoo.class )
						.withPropertyPath( pathWith()
								.property( "foos" )
								.bean( true, null, 0, List.class, 0 )

						),
				violationOf( ValidFoo.class )
						.withPropertyPath( pathWith()
								.property( "foos" )
								.bean( true, null, 1, List.class, 0 )

						)
		);
	}

	@Test
	public void testCascadedValidationViaTypeParameterOnGetter() {
		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<BarUsingTypeParameterOnGetter>> violations = validator.validate( new BarUsingTypeParameterOnGetter() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( ValidFoo.class )
						.withPropertyPath( pathWith()
								.property( "foos" )
								.bean( true, null, 0, List.class, 0 )

						),
				violationOf( ValidFoo.class )
						.withPropertyPath( pathWith()
								.property( "foos" )
								.bean( true, null, 1, List.class, 0 )

						)
		);
	}

	@ValidFoo
	private static class Foo {
	}

	private static class Bar {
		@Valid
		private final List<Foo> foos = Arrays.asList( new Foo(), new Foo() );
	}

	private static class BarUsingTypeParameterOnField {

		private final List<@Valid Foo> foos = Arrays.asList( new Foo(), new Foo() );
	}

	private static class BarUsingTypeParameterOnGetter {

		private List<@Valid Foo> getFoos() {
			return Arrays.asList( new Foo(), new Foo() );
		}
	}

	@Constraint(validatedBy = { ValidFooValidator.class })
	@Target({ TYPE })
	@Retention(RUNTIME)
	public @interface ValidFoo {
		String message() default "{ValidFoo.message}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class ValidFooValidator implements ConstraintValidator<ValidFoo, Foo> {

		@Override
		public boolean isValid(Foo foo, ConstraintValidatorContext context) {
			return false;
		}
	}
}
