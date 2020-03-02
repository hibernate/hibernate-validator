/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraintvalidator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
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

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

/**
 * @author Matthias Kurz
 */
@TestForIssue(jiraKey = "HV-1529")
public class ConstraintValidatorPayloadTest {

	@Test
	public void testHibernateValidatorContextPayload() {
		HibernateValidatorConfiguration configuration = getConfiguration();

		Validator validator = configuration.buildValidatorFactory().unwrap( HibernateValidatorFactory.class )
				.usingContext().constraintValidatorPayload( Integer.class )
				.getValidator();

		assertNoViolations( validator.validate( new PayloadDummyEntity( Integer.class ) ) );

		assertThat( validator.validate( new PayloadDummyEntity( String.class ) ) )
				.containsOnlyViolations( violationOf( PayloadConstraint.class ) );
	}

	@Test
	public void testHibernateValidatorConfigurationPayload() {
		HibernateValidatorConfiguration configuration = getConfiguration();

		configuration.constraintValidatorPayload( String.class );

		Validator validator = configuration.buildValidatorFactory().getValidator();

		assertNoViolations( validator.validate( new PayloadDummyEntity( String.class ) ) );

		assertThat( validator.validate( new PayloadDummyEntity( Integer.class ) ) )
				.containsOnlyViolations( violationOf( PayloadConstraint.class ) );
	}

	@Test
	public void testPayloadOverride() {
		HibernateValidatorConfiguration configuration = getConfiguration();

		configuration.constraintValidatorPayload( String.class );

		Validator validator = configuration.buildValidatorFactory().unwrap( HibernateValidatorFactory.class )
				.usingContext().constraintValidatorPayload( Integer.class )
				.getValidator();

		assertNoViolations( validator.validate( new PayloadDummyEntity( Integer.class ) ) );

		assertThat( validator.validate( new PayloadDummyEntity( String.class ) ) )
				.containsOnlyViolations( violationOf( PayloadConstraint.class ) );
	}

	private static HibernateValidatorConfiguration getConfiguration() {
		HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration();
		ConstraintMapping constraintMapping = configuration.createConstraintMapping();
		constraintMapping
				.constraintDefinition( PayloadConstraint.class )
				.validatedBy( PayloadContraintValidator.class );
		configuration.addMapping( constraintMapping );
		return configuration;
	}

	@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	@Constraint(validatedBy = { } )
	private @interface PayloadConstraint {
		String message() default "PayloadConstraint is not valid";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class PayloadContraintValidator implements ConstraintValidator<PayloadConstraint, Class<?>> {

		@Override
		public boolean isValid(Class<?> value, ConstraintValidatorContext context) {
			Class<?> payload = context.unwrap( HibernateConstraintValidatorContext.class ).getConstraintValidatorPayload( Class.class );
			if ( value != null && value.equals( payload ) ) {
				return true;
			}
			return false;
		}
	}

	private static class PayloadDummyEntity {

		@PayloadConstraint
		private Class<?> clazz;

		public PayloadDummyEntity(Class<?> clazz) {
			this.clazz = clazz;
		}
	}
}
