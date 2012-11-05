/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.internal.engine.constraintvalidation;

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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.GenericConstraintDef;
import org.hibernate.validator.testutil.TestForIssue;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getConfiguration;

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
		assertNumberOfViolations( constraintViolations, 1 );
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
		assertNumberOfViolations( constraintViolations, 1 );
	}

	/**
	 * As per the JLS, {@code Value<T>} is a sub-type of of the raw type
	 * {@code Value}. Therefore
	 * {@link ParametrizedValidatorForConstraintWithRawAndParametrizedValidator}
	 * is more specific than
	 * {@link RawValidatorForConstraintWithRawAndParametrizedValidator}.
	 *
	 * @see <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-4.html#jls-4.10.2">JLS</a> (subtyping)
	 * @see <a href="http://beanvalidation.org/1.1/spec/#typevalidatorresolution">BV spec</a> (constraint validator resolution)
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
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintViolationMessages(
				constraintViolations,
				"ParametrizedValidatorForConstraintWithRawAndParametrizedValidator"
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
		public void initialize(ConstraintWithParametrizedValidator constraint) {
			// nothing to initialize
		}

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
		public void initialize(ConstraintWithRawValidator constraint) {
			// nothing to initialize
		}

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
		public void initialize(ConstraintWithRawAndParametrizedValidator constraint) {
			// nothing to initialize
		}

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
		public void initialize(ConstraintWithRawAndParametrizedValidator constraint) {
			// nothing to initialize
		}

		@Override
		public boolean isValid(Value value, ConstraintValidatorContext context) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate( "RawValidatorForConstraintWithRawAndParametrizedValidator" )
					.addConstraintViolation();
			return false;
		}
	}
}
