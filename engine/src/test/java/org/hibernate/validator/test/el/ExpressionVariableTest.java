/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.el;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel.BEAN_METHODS;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.Validator;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Tom Gottfried
 */
@TestForIssue(jiraKey = "HV-2217")
public class ExpressionVariableTest {

	private Validator validator;

	@BeforeMethod
	public void init() {
		this.validator = ValidatorUtil.getValidator();
	}

	@Test
	public void nonNullValuedExpressionVariable() {
		assertThat( validator.validate( new TestBean( "value" ) ) )
				.containsOnlyViolations( violationOf( ExpressionVariableConstraint.class ).withMessage( "Variable value: value" ) );
	}

	@Test
	public void nullValuedExpressionVariable() {
		assertThat( validator.validate( new TestBean( null ) ) )
				.containsOnlyViolations( violationOf( ExpressionVariableConstraint.class ).withMessage( "Variable value: empty" ) );
	}


	@Target(FIELD)
	@Retention(RUNTIME)
	@Documented
	@Constraint(validatedBy = { ExpressionVariableConstraintValidator.class })
	private @interface ExpressionVariableConstraint {
		String message() default "Variable value: ${empty v ? 'empty' : v}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class ExpressionVariableConstraintValidator implements ConstraintValidator<ExpressionVariableConstraint, String> {

		private String message;

		@Override
		public void initialize(ExpressionVariableConstraint constraintAnnotation) {
			this.message = constraintAnnotation.message();
		}

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			HibernateConstraintValidatorContext hibernateCtx = context.unwrap(
					HibernateConstraintValidatorContext.class
			);
			hibernateCtx.disableDefaultConstraintViolation();
			hibernateCtx
					.addExpressionVariable( "v", value )
					.buildConstraintViolationWithTemplate( this.message )
					.enableExpressionLanguage( BEAN_METHODS )
					.addConstraintViolation();
			return false;
		}
	}

	public static class TestBean {

		public TestBean(String value) {
			this.value = value;
		}

		@ExpressionVariableConstraint
		public String value;
	}
}
