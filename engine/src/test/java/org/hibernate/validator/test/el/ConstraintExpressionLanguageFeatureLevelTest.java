/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.el;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidationXmlTestHelper;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Guillaume Smet
 */
@TestForIssue(jiraKey = "HV-1816")
public class ConstraintExpressionLanguageFeatureLevelTest {

	private static ValidationXmlTestHelper validationXmlTestHelper;

	@BeforeClass
	public static void setupValidationXmlTestHelper() {
		validationXmlTestHelper = new ValidationXmlTestHelper( ConstraintExpressionLanguageFeatureLevelTest.class );
	}

	@Test
	public void default_expression_language_feature_level() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();

		assertThat( validator.validate( new VariablesBean( "value" ) ) )
				.containsOnlyViolations( violationOf( VariablesConstraint.class ).withMessage( "Variable: value" ) );
		assertThat( validator.validate( new BeanPropertiesBean( "value" ) ) )
				.containsOnlyViolations( violationOf( BeanPropertiesConstraint.class ).withMessage( "Bean property: 118" ) );
		assertThat( validator.validate( new BeanMethodsBean() ) )
				.containsOnlyViolations( violationOf( BeanMethodsConstraint.class ).withMessage( "Method execution: ${'aaaa'.substring(0, 1)}" ) );
	}

	@Test
	public void none_expression_language_feature_level() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.constraintExpressionLanguageFeatureLevel( ExpressionLanguageFeatureLevel.NONE )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();

		assertThat( validator.validate( new VariablesBean( "value" ) ) )
				.containsOnlyViolations( violationOf( VariablesConstraint.class ).withMessage( "Variable: ${validatedValue}" ) );
		assertThat( validator.validate( new BeanPropertiesBean( "value" ) ) )
				.containsOnlyViolations( violationOf( BeanPropertiesConstraint.class ).withMessage( "Bean property: ${validatedValue.bytes[0]}" ) );
		assertThat( validator.validate( new BeanMethodsBean() ) )
				.containsOnlyViolations( violationOf( BeanMethodsConstraint.class ).withMessage( "Method execution: ${'aaaa'.substring(0, 1)}" ) );
	}

	@Test
	public void variables_expression_language_feature_level() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.constraintExpressionLanguageFeatureLevel( ExpressionLanguageFeatureLevel.VARIABLES )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();

		assertThat( validator.validate( new VariablesBean( "value" ) ) )
				.containsOnlyViolations( violationOf( VariablesConstraint.class ).withMessage( "Variable: value" ) );
		assertThat( validator.validate( new BeanPropertiesBean( "value" ) ) )
				.containsOnlyViolations( violationOf( BeanPropertiesConstraint.class ).withMessage( "Bean property: ${validatedValue.bytes[0]}" ) );
		assertThat( validator.validate( new BeanMethodsBean() ) )
				.containsOnlyViolations( violationOf( BeanMethodsConstraint.class ).withMessage( "Method execution: ${'aaaa'.substring(0, 1)}" ) );
	}

	@Test
	public void bean_properties_expression_language_feature_level() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.constraintExpressionLanguageFeatureLevel( ExpressionLanguageFeatureLevel.BEAN_PROPERTIES )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();

		assertThat( validator.validate( new VariablesBean( "value" ) ) )
				.containsOnlyViolations( violationOf( VariablesConstraint.class ).withMessage( "Variable: value" ) );
		assertThat( validator.validate( new BeanPropertiesBean( "value" ) ) )
				.containsOnlyViolations( violationOf( BeanPropertiesConstraint.class ).withMessage( "Bean property: 118" ) );
		assertThat( validator.validate( new BeanMethodsBean() ) )
				.containsOnlyViolations( violationOf( BeanMethodsConstraint.class ).withMessage( "Method execution: ${'aaaa'.substring(0, 1)}" ) );
	}

	@Test
	public void bean_methods_expression_language_feature_level() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.constraintExpressionLanguageFeatureLevel( ExpressionLanguageFeatureLevel.BEAN_METHODS )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();

		assertThat( validator.validate( new VariablesBean( "value" ) ) )
				.containsOnlyViolations( violationOf( VariablesConstraint.class ).withMessage( "Variable: value" ) );
		assertThat( validator.validate( new BeanPropertiesBean( "value" ) ) )
				.containsOnlyViolations( violationOf( BeanPropertiesConstraint.class ).withMessage( "Bean property: 118" ) );
		assertThat( validator.validate( new BeanMethodsBean() ) )
				.containsOnlyViolations( violationOf( BeanMethodsConstraint.class ).withMessage( "Method execution: a" ) );
	}

	@Test
	public void property_default_value() {
		validationXmlTestHelper.runWithCustomValidationXml(
				"validation-constraints-default.xml", new Runnable() {

					@Override
					public void run() {
						Validator validator = ValidatorUtil.getValidator();

						assertThat( validator.validate( new VariablesBean( "value" ) ) )
								.containsOnlyViolations( violationOf( VariablesConstraint.class ).withMessage( "Variable: value" ) );
						assertThat( validator.validate( new BeanPropertiesBean( "value" ) ) )
								.containsOnlyViolations( violationOf( BeanPropertiesConstraint.class ).withMessage( "Bean property: 118" ) );
						assertThat( validator.validate( new BeanMethodsBean() ) )
								.containsOnlyViolations( violationOf( BeanMethodsConstraint.class ).withMessage( "Method execution: ${'aaaa'.substring(0, 1)}" ) );
					}
				} );
	}

	@Test
	public void property_bean_methods() {
		validationXmlTestHelper.runWithCustomValidationXml(
				"validation-constraints-bean-methods.xml", new Runnable() {

					@Override
					public void run() {
						Validator validator = ValidatorUtil.getValidator();

						assertThat( validator.validate( new VariablesBean( "value" ) ) )
						.containsOnlyViolations( violationOf( VariablesConstraint.class ).withMessage( "Variable: value" ) );
						assertThat( validator.validate( new BeanPropertiesBean( "value" ) ) )
								.containsOnlyViolations( violationOf( BeanPropertiesConstraint.class ).withMessage( "Bean property: 118" ) );
						assertThat( validator.validate( new BeanMethodsBean() ) )
								.containsOnlyViolations( violationOf( BeanMethodsConstraint.class ).withMessage( "Method execution: a" ) );
					}
				} );
	}

	@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	@Constraint(validatedBy = { VariablesStringValidator.class })
	private @interface VariablesConstraint {
		String message() default "Variable: ${validatedValue}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class VariablesStringValidator implements ConstraintValidator<VariablesConstraint, String> {

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			return false;
		}
	}

	public static class VariablesBean {

		public VariablesBean(String value) {
			this.value = value;
		}

		@VariablesConstraint
		public String value;
	}

	@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	@Constraint(validatedBy = { BeanPropertiesConstraintStringValidator.class })
	private @interface BeanPropertiesConstraint {
		String message() default "Bean property: ${validatedValue.bytes[0]}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class BeanPropertiesConstraintStringValidator implements ConstraintValidator<BeanPropertiesConstraint, String> {

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			return false;
		}
	}

	public static class BeanPropertiesBean {

		public BeanPropertiesBean(String value) {
			this.value = value;
		}

		@BeanPropertiesConstraint
		public String value;
	}

	@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	@Constraint(validatedBy = { BeanMethodsConstraintStringValidator.class })
	private @interface BeanMethodsConstraint {
		String message() default "Method execution: ${'aaaa'.substring(0, 1)}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class BeanMethodsConstraintStringValidator implements ConstraintValidator<BeanMethodsConstraint, String> {

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			return false;
		}
	}

	public static class BeanMethodsBean {

		@BeanMethodsConstraint
		public String value;
	}
}
