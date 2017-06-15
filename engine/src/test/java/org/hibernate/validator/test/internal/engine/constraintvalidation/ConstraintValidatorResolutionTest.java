/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.constraintvalidation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.Validator;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.GenericConstraintDef;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class ConstraintValidatorResolutionTest {

	private HibernateValidatorConfiguration configuration;

	private ConstraintMapping constraintMapping;

	@BeforeMethod
	public void setupMapping() {

		configuration = getConfiguration();

		constraintMapping = configuration.createConstraintMapping();
		configuration.addMapping( constraintMapping );
	}

	@Test
	@TestForIssue(jiraKey = "HV-623")
	public void validatorForParametrizedTypeIsCorrectlyResolved() {

		//given
		constraintMapping.type( Value.class )
				.constraint(
						new GenericConstraintDef<ConstraintWithParametrizedValidator>(
								ConstraintWithParametrizedValidator.class
						)
				);

		Validator validator = configuration.buildValidatorFactory().getValidator();

		//when
		Set<ConstraintViolation<Value<Integer>>> constraintViolations = validator.validate( new Value<Integer>() );

		//then
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ConstraintWithParametrizedValidator.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-623")
	public void validatorForRawTypeIsCorrectlyResolved() {

		//given
		constraintMapping.type( Value.class )
				.constraint( new GenericConstraintDef<ConstraintWithRawValidator>( ConstraintWithRawValidator.class ) );

		Validator validator = configuration.buildValidatorFactory().getValidator();

		//when
		Set<ConstraintViolation<Value<Integer>>> constraintViolations = validator.validate( new Value<Integer>() );

		//then
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ConstraintWithRawValidator.class )
		);
	}

	/**
	 * As per the JLS, {@code Value<T>} is a sub-type of of the raw type
	 * {@code Value}. Therefore
	 * {@link ParametrizedValidatorForConstraintWithRawAndParametrizedValidator}
	 * is more specific than
	 * {@link RawValidatorForConstraintWithRawAndParametrizedValidator}.
	 *
	 * @see <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.10.2">JLS</a> (subtyping)
	 * @see <a href="http://beanvalidation.org/2.0/spec/#typevalidatorresolution">BV spec</a> (constraint validator resolution)
	 */
	@Test
	@TestForIssue(jiraKey = "HV-623")

	public void parametrizedValidatorHasPrecedenceOverRawValidator() {

		//given
		constraintMapping.type( Value.class )
				.constraint(
						new GenericConstraintDef<ConstraintWithRawAndParametrizedValidator>(
								ConstraintWithRawAndParametrizedValidator.class
						)
				);

		Validator validator = configuration.buildValidatorFactory().getValidator();

		//when
		Set<ConstraintViolation<Value<Integer>>> constraintViolations = validator.validate( new Value<Integer>() );

		//then
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ConstraintWithRawAndParametrizedValidator.class ).withMessage( "ParametrizedValidatorForConstraintWithRawAndParametrizedValidator" )
		);
	}

	public class Value<T> {
	}

	@Target({ TYPE, ANNOTATION_TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = ParametrizedValidator.class)
	@Documented
	public @interface ConstraintWithParametrizedValidator {
		String message() default "foo";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class ParametrizedValidator
			implements ConstraintValidator<ConstraintWithParametrizedValidator, Value<?>> {

		@Override
		public boolean isValid(Value<?> settingValue, ConstraintValidatorContext context) {
			return false;
		}
	}

	@Target({ TYPE, ANNOTATION_TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = RawValidator.class)
	@Documented
	public @interface ConstraintWithRawValidator {
		String message() default "foo";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	@SuppressWarnings("rawtypes")
	public static class RawValidator implements ConstraintValidator<ConstraintWithRawValidator, Value> {

		@Override
		public boolean isValid(Value value, ConstraintValidatorContext context) {
			return false;
		}
	}

	@Target({ TYPE, ANNOTATION_TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = {
			RawValidatorForConstraintWithRawAndParametrizedValidator.class,
			ParametrizedValidatorForConstraintWithRawAndParametrizedValidator.class
	})
	@Documented
	public @interface ConstraintWithRawAndParametrizedValidator {
		String message() default "foo";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class ParametrizedValidatorForConstraintWithRawAndParametrizedValidator
			implements ConstraintValidator<ConstraintWithRawAndParametrizedValidator, Value<?>> {

		@Override
		public boolean isValid(Value<?> value, ConstraintValidatorContext context) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(
					"ParametrizedValidatorForConstraintWithRawAndParametrizedValidator"
			).addConstraintViolation();
			return false;
		}
	}

	@SuppressWarnings("rawtypes")
	public static class RawValidatorForConstraintWithRawAndParametrizedValidator
			implements ConstraintValidator<ConstraintWithRawAndParametrizedValidator, Value> {

		@Override
		public boolean isValid(Value value, ConstraintValidatorContext context) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate( "RawValidatorForConstraintWithRawAndParametrizedValidator" )
					.addConstraintViolation();
			return false;
		}
	}
}
