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
import static org.testng.Assert.assertTrue;

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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.test.appender.ListAppender;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidationXmlTestHelper;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Guillaume Smet
 */
@TestForIssue(jiraKey = "HV-1816")
public class CustomViolationExpressionLanguageFeatureLevelTest {

	private static ValidationXmlTestHelper validationXmlTestHelper;

	private ListAppender constraintValidatorContextImplLoglistAppender;


	@BeforeClass
	public static void setupValidationXmlTestHelper() {
		validationXmlTestHelper = new ValidationXmlTestHelper( ConstraintExpressionLanguageFeatureLevelTest.class );
	}

	@BeforeTest
	public void setUp() {
		LoggerContext context = LoggerContext.getContext( false );
		Logger logger = context.getLogger( ConstraintValidatorContextImpl.class.getName() );
		constraintValidatorContextImplLoglistAppender = (ListAppender) logger.getAppenders().get( "List" );
		constraintValidatorContextImplLoglistAppender.clear();
	}

	@AfterTest
	public void tearDown() {
		constraintValidatorContextImplLoglistAppender.clear();
	}

	@Test
	public void default_behavior() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();

		assertThat( validator.validate( new DefaultLevelBean() ) )
				.containsOnlyViolations( violationOf( DefaultLevelConstraint.class ).withMessage( "Variable: ${validatedValue}" ),
						violationOf( DefaultLevelConstraint.class ).withMessage( "Bean property: ${validatedValue.bytes[0]}" ),
						violationOf( DefaultLevelConstraint.class ).withMessage( "Method execution: ${'aaaa'.substring(0, 1)}" ) );
	}

	@Test
	public void enable_el() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();

		assertThat( validator.validate( new EnableELBean( "value" ) ) )
				.containsOnlyViolations( violationOf( EnableELConstraint.class ).withMessage( "Variable: value" ),
						violationOf( EnableELConstraint.class ).withMessage( "Bean property: ${validatedValue.bytes[0]}" ),
						violationOf( EnableELConstraint.class ).withMessage( "Method execution: ${'aaaa'.substring(0, 1)}" ) );
	}

	@Test
	public void enable_el_bean_properties() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();

		assertThat( validator.validate( new EnableELBeanPropertiesBean( "value" ) ) )
				.containsOnlyViolations( violationOf( EnableELBeanPropertiesConstraint.class ).withMessage( "Variable: value" ),
						violationOf( EnableELBeanPropertiesConstraint.class ).withMessage( "Bean property: 118" ),
						violationOf( EnableELBeanPropertiesConstraint.class ).withMessage( "Method execution: ${'aaaa'.substring(0, 1)}" ) );
	}

	@Test
	public void enable_el_bean_methods() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();

		assertThat( validator.validate( new EnableELBeanMethodsBean( "value" ) ) )
				.containsOnlyViolations( violationOf( EnableELBeanMethodsConstraint.class ).withMessage( "Variable: value" ),
						violationOf( EnableELBeanMethodsConstraint.class ).withMessage( "Bean property: 118" ),
						violationOf( EnableELBeanMethodsConstraint.class ).withMessage( "Method execution: a" ) );
	}

	@Test
	public void warn_when_default_behavior_and_expression_variables() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();

		assertThat( validator.validate( new DefaultLevelWithExpressionVariablesBean() ) )
				.containsOnlyViolations( violationOf( DefaultLevelWithExpressionVariablesConstraint.class ).withMessage( "Variable: ${myVariable}" ) );

		assertTrue( constraintValidatorContextImplLoglistAppender.getEvents().stream()
				.filter( event -> event.getLevel().equals( Level.WARN ) )
				.map( event -> event.getMessage().getFormattedMessage() )
				.anyMatch( m -> m.startsWith( "HV000257" ) ) );
	}

	@Test
	public void property_default_value() {
		validationXmlTestHelper.runWithCustomValidationXml(
				"validation-custom-violations-default.xml", new Runnable() {

					@Override
					public void run() {
						Validator validator = ValidatorUtil.getValidator();

						assertThat( validator.validate( new EnableELBean( "value" ) ) )
								.containsOnlyViolations( violationOf( EnableELConstraint.class ).withMessage( "Variable: value" ),
										violationOf( EnableELConstraint.class ).withMessage( "Bean property: ${validatedValue.bytes[0]}" ),
										violationOf( EnableELConstraint.class ).withMessage( "Method execution: ${'aaaa'.substring(0, 1)}" ) );
					}
				} );
	}

	@Test
	public void property_bean_methods() {
		validationXmlTestHelper.runWithCustomValidationXml(
				"validation-custom-violations-bean-methods.xml", new Runnable() {

					@Override
					public void run() {
						Validator validator = ValidatorUtil.getValidator();

						assertThat( validator.validate( new EnableELBeanMethodsBean( "value" ) ) )
								.containsOnlyViolations( violationOf( EnableELBeanMethodsConstraint.class ).withMessage( "Variable: value" ),
										violationOf( EnableELBeanMethodsConstraint.class ).withMessage( "Bean property: 118" ),
										violationOf( EnableELBeanMethodsConstraint.class ).withMessage( "Method execution: a" ) );
					}
				} );
	}

	@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	@Constraint(validatedBy = { DefaultLevelStringValidator.class })
	private @interface DefaultLevelConstraint {

		String message() default "-";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};
	}

	public static class DefaultLevelStringValidator implements ConstraintValidator<DefaultLevelConstraint, String> {

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			HibernateConstraintValidatorContext hibernateContext = (HibernateConstraintValidatorContext) context;

			hibernateContext.disableDefaultConstraintViolation();

			hibernateContext.buildConstraintViolationWithTemplate( "Variable: ${validatedValue}" ).addConstraintViolation();
			hibernateContext.buildConstraintViolationWithTemplate( "Bean property: ${validatedValue.bytes[0]}" ).addConstraintViolation();
			hibernateContext.buildConstraintViolationWithTemplate( "Method execution: ${'aaaa'.substring(0, 1)}" ).addConstraintViolation();

			return false;
		}
	}

	public static class DefaultLevelBean {

		@DefaultLevelConstraint
		public String value;
	}

	@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	@Constraint(validatedBy = { EnableELStringValidator.class })
	private @interface EnableELConstraint {

		String message() default "-";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};
	}

	public static class EnableELStringValidator implements ConstraintValidator<EnableELConstraint, String> {

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			HibernateConstraintValidatorContext hibernateContext = (HibernateConstraintValidatorContext) context;

			hibernateContext.disableDefaultConstraintViolation();

			hibernateContext.buildConstraintViolationWithTemplate( "Variable: ${validatedValue}" )
					.enableExpressionLanguage()
					.addConstraintViolation();
			hibernateContext.buildConstraintViolationWithTemplate( "Bean property: ${validatedValue.bytes[0]}" )
					.enableExpressionLanguage()
					.addConstraintViolation();
			hibernateContext.buildConstraintViolationWithTemplate( "Method execution: ${'aaaa'.substring(0, 1)}" )
					.enableExpressionLanguage()
					.addConstraintViolation();

			return false;
		}
	}

	public static class EnableELBean {

		public EnableELBean(String value) {
			this.value = value;
		}

		@EnableELConstraint
		public String value;
	}

	@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	@Constraint(validatedBy = { EnableELBeanPropertiesStringValidator.class })
	private @interface EnableELBeanPropertiesConstraint {

		String message() default "-";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};
	}

	public static class EnableELBeanPropertiesStringValidator implements ConstraintValidator<EnableELBeanPropertiesConstraint, String> {

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			HibernateConstraintValidatorContext hibernateContext = (HibernateConstraintValidatorContext) context;

			hibernateContext.disableDefaultConstraintViolation();

			hibernateContext.buildConstraintViolationWithTemplate( "Variable: ${validatedValue}" )
					.enableExpressionLanguage( ExpressionLanguageFeatureLevel.BEAN_PROPERTIES )
					.addConstraintViolation();
			hibernateContext.buildConstraintViolationWithTemplate( "Bean property: ${validatedValue.bytes[0]}" )
					.enableExpressionLanguage( ExpressionLanguageFeatureLevel.BEAN_PROPERTIES )
					.addConstraintViolation();
			hibernateContext.buildConstraintViolationWithTemplate( "Method execution: ${'aaaa'.substring(0, 1)}" )
					.enableExpressionLanguage( ExpressionLanguageFeatureLevel.BEAN_PROPERTIES )
					.addConstraintViolation();

			return false;
		}
	}

	public static class EnableELBeanPropertiesBean {

		public EnableELBeanPropertiesBean(String value) {
			this.value = value;
		}

		@EnableELBeanPropertiesConstraint
		public String value;
	}

	@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	@Constraint(validatedBy = { EnableELBeanMethodsStringValidator.class })
	private @interface EnableELBeanMethodsConstraint {

		String message() default "-";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};
	}

	public static class EnableELBeanMethodsStringValidator implements ConstraintValidator<EnableELBeanMethodsConstraint, String> {

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			HibernateConstraintValidatorContext hibernateContext = (HibernateConstraintValidatorContext) context;

			hibernateContext.disableDefaultConstraintViolation();

			hibernateContext.buildConstraintViolationWithTemplate( "Variable: ${validatedValue}" )
					.enableExpressionLanguage( ExpressionLanguageFeatureLevel.BEAN_METHODS )
					.addConstraintViolation();
			hibernateContext.buildConstraintViolationWithTemplate( "Bean property: ${validatedValue.bytes[0]}" )
					.enableExpressionLanguage( ExpressionLanguageFeatureLevel.BEAN_METHODS )
					.addConstraintViolation();
			hibernateContext.buildConstraintViolationWithTemplate( "Method execution: ${'aaaa'.substring(0, 1)}" )
					.enableExpressionLanguage( ExpressionLanguageFeatureLevel.BEAN_METHODS )
					.addConstraintViolation();

			return false;
		}
	}

	public static class EnableELBeanMethodsBean {

		public EnableELBeanMethodsBean(String value) {
			this.value = value;
		}

		@EnableELBeanMethodsConstraint
		public String value;
	}

	@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	@Constraint(validatedBy = { DefaultLevelWithExpressionVariablesStringValidator.class })
	private @interface DefaultLevelWithExpressionVariablesConstraint {

		String message() default "-";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};
	}

	public static class DefaultLevelWithExpressionVariablesStringValidator
			implements ConstraintValidator<DefaultLevelWithExpressionVariablesConstraint, String> {

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			HibernateConstraintValidatorContext hibernateContext = (HibernateConstraintValidatorContext) context;

			hibernateContext.disableDefaultConstraintViolation();

			hibernateContext
					.addExpressionVariable( "myVariable", "value" )
					.buildConstraintViolationWithTemplate( "Variable: ${myVariable}" )
					.addConstraintViolation();

			return false;
		}
	}

	public static class DefaultLevelWithExpressionVariablesBean {

		@DefaultLevelWithExpressionVariablesConstraint
		public String value;
	}
}
