/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.messageinterpolation.MessageParameterEscaping;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

public class MessageParameterEscapingTest {

	@Test
	public void stringValueIsEscapedByDefault() {
		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<DefaultEscapingBean>> violations = validator.validate(
				new DefaultEscapingBean( "plain text" ) );

		assertThat( violations ).containsOnlyViolations(
				violationOf( EscapingConstraint.class ).withMessage( "value: plain text" )
		);
	}

	@Test
	public void curlyBracesInValueAreEscapedByDefault() {
		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<DefaultEscapingBean>> violations = validator.validate(
				new DefaultEscapingBean( "{dangerous}" ) );

		assertThat( violations ).containsOnlyViolations(
				violationOf( EscapingConstraint.class ).withMessage( "value: {dangerous}" )
		);
	}

	@Test
	public void dollarCurlyInValueIsEscapedByDefault() {
		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<DefaultEscapingBean>> violations = validator.validate(
				new DefaultEscapingBean( "${evil}" ) );

		assertThat( violations ).containsOnlyViolations(
				violationOf( EscapingConstraint.class ).withMessage( "value: ${evil}" )
		);
	}

	@Test
	public void backslashInValueIsEscapedByDefault() {
		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<DefaultEscapingBean>> violations = validator.validate(
				new DefaultEscapingBean( "back\\slash" ) );

		assertThat( violations ).containsOnlyViolations(
				violationOf( EscapingConstraint.class ).withMessage( "value: back\\slash" )
		);
	}

	@Test
	public void numberValueIsNeverEscaped() {
		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<NumberBean>> violations = validator.validate( new NumberBean() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( EscapingConstraint.class ).withMessage( "value: 42" )
		);
	}

	@Test
	public void nullValueIsHandled() {
		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<NullBean>> violations = validator.validate( new NullBean() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( EscapingConstraint.class ).withMessage( "value: {v}" )
		);
	}

	@Test
	public void noEscapeOverloadPassesValueAsIs() {
		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<NoEscapeBean>> violations = validator.validate(
				new NoEscapeBean( "already\\escaped" ) );

		assertThat( violations ).containsOnlyViolations(
				violationOf( NoEscapeConstraint.class ).withMessage( "value: already\\escaped" )
		);
	}

	@Test
	public void noEscapeOverloadWithCurlyBracesLeavesThemUnescaped() {
		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<NoEscapeBean>> violations = validator.validate(
				new NoEscapeBean( "{v}" ) );

		assertThat( violations ).containsOnlyViolations(
				violationOf( NoEscapeConstraint.class ).withMessage( "value: {v}" )
		);
	}

	@Test
	public void escapeOverloadEscapesValue() {
		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<ExplicitEscapeBean>> violations = validator.validate(
				new ExplicitEscapeBean( "${inject}" ) );

		assertThat( violations ).containsOnlyViolations(
				violationOf( ExplicitEscapeConstraint.class ).withMessage( "value: ${inject}" )
		);
	}

	@Test
	public void noEscapeViaPropertyConfig() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.messageParameterEscaping( MessageParameterEscaping.NONE )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<DefaultEscapingBean>> violations = validator.validate(
				new DefaultEscapingBean( "safe" ) );

		assertThat( violations ).containsOnlyViolations(
				violationOf( EscapingConstraint.class ).withMessage( "value: safe" )
		);
	}

	@Test
	public void noEscapeViaPropertyConfigDoesNotEscapeCurlyBraces() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.messageParameterEscaping( MessageParameterEscaping.NONE )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<DefaultEscapingBean>> violations = validator.validate(
				new DefaultEscapingBean( "{v}" ) );

		// With NONE config, {v} is stored raw and will be treated as interpolation token
		assertThat( violations ).containsOnlyViolations(
				violationOf( EscapingConstraint.class ).withMessage( "value: {v}" )
		);
	}

	@Test
	public void escapeOverloadOverridesNoEscapeConfig() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.messageParameterEscaping( MessageParameterEscaping.NONE )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<ExplicitEscapeBean>> violations = validator.validate(
				new ExplicitEscapeBean( "${inject}" ) );

		// Even though config is NONE, the explicit ESCAPE overload should escape
		assertThat( violations ).containsOnlyViolations(
				violationOf( ExplicitEscapeConstraint.class ).withMessage( "value: ${inject}" )
		);
	}

	@Test
	public void noEscapeViaPropertyString() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addProperty( "hibernate.validator.message_parameter_escaping", "none" )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<DefaultEscapingBean>> violations = validator.validate(
				new DefaultEscapingBean( "safe" ) );

		assertThat( violations ).containsOnlyViolations(
				violationOf( EscapingConstraint.class ).withMessage( "value: safe" )
		);
	}

	@Test
	public void escapingPreventsElExpressionEvaluation() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<DefaultEscapingBean>> violations = validator.validate(
				new DefaultEscapingBean( "${1+1}" ) );

		assertThat( violations ).containsOnlyViolations(
				violationOf( EscapingConstraint.class ).withMessage( "value: ${1+1}" )
		);
	}

	@Test
	public void noEscapeAllowsElExpressionEvaluation() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.messageParameterEscaping( MessageParameterEscaping.NONE )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<DefaultEscapingBean>> violations = validator.validate(
				new DefaultEscapingBean( "${1+1}" ) );

		assertThat( violations ).containsOnlyViolations(
				violationOf( EscapingConstraint.class ).withMessage( "value: 2" )
		);
	}

	@Test
	public void escapingPreventsElMethodInjection() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<DefaultEscapingBean>> violations = validator.validate(
				new DefaultEscapingBean( "${'injected'.toUpperCase()}" ) );

		assertThat( violations ).containsOnlyViolations(
				violationOf( EscapingConstraint.class ).withMessage( "value: ${'injected'.toUpperCase()}" )
		);
	}

	@Test
	public void noEscapeAllowsElMethodInjection() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.messageParameterEscaping( MessageParameterEscaping.NONE )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<DefaultEscapingBean>> violations = validator.validate(
				new DefaultEscapingBean( "${'injected'.toUpperCase()}" ) );

		assertThat( violations ).containsOnlyViolations(
				violationOf( EscapingConstraint.class ).withMessage( "value: INJECTED" )
		);
	}

	public static class DefaultEscapingBean {
		@EscapingConstraint
		private final String input;

		public DefaultEscapingBean(String input) {
			this.input = input;
		}
	}

	public static class NumberBean {
		@EscapingConstraint
		private final String input = "unused";

		public NumberBean() {
		}
	}

	public static class NullBean {
		@EscapingConstraint
		private final String input = "null-test";

		public NullBean() {
		}
	}

	public static class NoEscapeBean {
		@NoEscapeConstraint
		private final String input;

		public NoEscapeBean(String input) {
			this.input = input;
		}
	}

	public static class ExplicitEscapeBean {
		@ExplicitEscapeConstraint
		private final String input;

		public ExplicitEscapeBean(String input) {
			this.input = input;
		}
	}

	@Target(FIELD)
	@Retention(RUNTIME)
	@Documented
	@Constraint(validatedBy = EscapingConstraintValidator.class)
	public @interface EscapingConstraint {
		String message() default "value: {v}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class EscapingConstraintValidator implements ConstraintValidator<EscapingConstraint, String> {

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			HibernateConstraintValidatorContext hibernateCtx = context.unwrap( HibernateConstraintValidatorContext.class );

			if ( "unused".equals( value ) ) {
				// Number test
				hibernateCtx.addMessageParameter( "v", 42 );
			}
			else if ( "null-test".equals( value ) ) {
				// null test — don't add parameter, let template show {v}
			}
			else {
				hibernateCtx.addMessageParameter( "v", value );
			}

			return false;
		}
	}

	@Target(FIELD)
	@Retention(RUNTIME)
	@Documented
	@Constraint(validatedBy = NoEscapeConstraintValidator.class)
	public @interface NoEscapeConstraint {
		String message() default "value: {v}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class NoEscapeConstraintValidator implements ConstraintValidator<NoEscapeConstraint, String> {

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			HibernateConstraintValidatorContext hibernateCtx = context.unwrap( HibernateConstraintValidatorContext.class );
			hibernateCtx.addMessageParameter( "v", value, MessageParameterEscaping.NONE );
			return false;
		}
	}

	@Target(FIELD)
	@Retention(RUNTIME)
	@Documented
	@Constraint(validatedBy = ExplicitEscapeConstraintValidator.class)
	public @interface ExplicitEscapeConstraint {
		String message() default "value: {v}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class ExplicitEscapeConstraintValidator implements ConstraintValidator<ExplicitEscapeConstraint, String> {

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			HibernateConstraintValidatorContext hibernateCtx = context.unwrap( HibernateConstraintValidatorContext.class );
			hibernateCtx.addMessageParameter( "v", value, MessageParameterEscaping.ESCAPE );
			return false;
		}
	}
}
