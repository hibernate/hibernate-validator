/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter09;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;
import java.util.function.BiConsumer;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.bean.BeanRetrieval;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.constraints.PasswordPolicy;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.spi.password.AbstractPasswordPolicyValidator;
import org.hibernate.validator.spi.password.CharacterType;
import org.hibernate.validator.spi.password.PasswordContext;
import org.hibernate.validator.spi.password.PasswordPolicyBuilder;
import org.hibernate.validator.spi.password.PasswordPolicyDefinition;
import org.hibernate.validator.spi.password.PasswordPolicyRule;

import org.junit.Test;

public class PasswordPolicyTest {

	//tag::policyDefinition[]
	public static class WebAppPolicy implements PasswordPolicyDefinition {

		@Override
		public void configure(PasswordPolicyBuilder builder,
				HibernateConstraintValidatorInitializationContext context) {
			builder.minLength( 8 )
					.requireCharacters( CharacterType.UPPERCASE, 1 )
					.requireCharacters( CharacterType.DIGIT, 1 )
					.noWhitespace();
		}
	}
	//end::policyDefinition[]

	//tag::policyUsage[]
	public static class UserAccount {

		@PasswordPolicy(WebAppPolicy.class)
		private final String password;

		public UserAccount(String password) {
			this.password = password;
		}
	}
	//end::policyUsage[]

	//tag::lambdaRules[]
	public static class StrictPolicy implements PasswordPolicyDefinition {

		@Override
		public void configure(PasswordPolicyBuilder builder,
				HibernateConstraintValidatorInitializationContext context) {
			builder.minLength( 8 )
					// reject passwords containing the word "password"
					.addRule( "must not contain 'password'",
							pw -> !new String( pw ).toLowerCase().contains( "password" ) )
					// reject passwords containing a year (1900-2099)
					.addRule( "must not contain a year",
							pw -> !new String( pw ).matches( ".*(?:19|20)\\d{2}.*" ) )
					// no single character more than 3 times total
					.addRule( "no character may appear more than 3 times",
							pw -> {
								int[] counts = new int[Character.MAX_VALUE + 1];
								for ( char c : pw ) {
									if ( ++counts[c] > 3 ) {
										return false;
									}
								}
								return true;
							} );
		}
	}
	//end::lambdaRules[]

	//tag::serviceRules[]
	public static class EnterprisePolicy implements PasswordPolicyDefinition {

		@Override
		public void configure(PasswordPolicyBuilder builder,
				HibernateConstraintValidatorInitializationContext context) {
			PasswordHistoryService historyService = context.getBeanResolver()
					.resolve( PasswordHistoryService.class, BeanRetrieval.ANY ).get();
			builder.minLength( 8 )
					.noSequence()
					.addRule( new PasswordHistoryRule( historyService ) );
		}
	}
	//end::serviceRules[]

	@Test
	public void basicPolicy() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<UserAccount>> violations = validator.validate(
				new UserAccount( "Passw0rd!" ) );
		assertNoViolations( violations );

		violations = validator.validate( new UserAccount( "ab" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( PasswordPolicy.class ),
				violationOf( PasswordPolicy.class ),
				violationOf( PasswordPolicy.class )
		);
	}

	@Test
	public void lambdaRules() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<StrictPolicyBean>> violations = validator.validate(
				new StrictPolicyBean( "mypassword" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( PasswordPolicy.class )
		);

		violations = validator.validate( new StrictPolicyBean( "secure2025" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( PasswordPolicy.class )
		);

		violations = validator.validate( new StrictPolicyBean( "S3cureV@lue" ) );
		assertNoViolations( violations );
	}

	private static class StrictPolicyBean {
		@PasswordPolicy(StrictPolicy.class)
		private final String password;

		StrictPolicyBean(String password) {
			this.password = password;
		}
	}

	//tag::classLevelPolicy[]
	public static class RegistrationPolicy implements PasswordPolicyDefinition {

		@Override
		public void configure(PasswordPolicyBuilder builder,
				HibernateConstraintValidatorInitializationContext context) {
			builder.minLength( 8 )
					.requireCharacters( CharacterType.UPPERCASE, 1 )
					.requireCharacters( CharacterType.DIGIT, 1 )
					.addRule( new NoUsernameInPasswordRule() );
		}
	}
	//end::classLevelPolicy[]

	//tag::noUsernameRule[]
	public static class NoUsernameInPasswordRule implements PasswordPolicyRule {

		@Override
		public String getMessage() {
			return "password must not contain the username '{username}'";
		}

		@Override
		public boolean isValid(PasswordContext passwordContext,
				HibernateConstraintValidatorContext context) {
			String username = passwordContext.get( "username", String.class );
			if ( username == null ) {
				return true;
			}
			context.addMessageParameter( "username", username );
			return !new String( passwordContext.password() )
					.toLowerCase().contains( username.toLowerCase() );
		}
	}
	//end::noUsernameRule[]

	//tag::classLevelBean[]
	@PasswordPolicy(RegistrationPolicy.class)
	public static class RegistrationForm {

		private final String username;
		private final String password;

		public RegistrationForm(String username, String password) {
			this.username = username;
			this.password = password;
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}
	}
	//end::classLevelBean[]

	//tag::classLevelValidator[]
	public static class RegistrationFormPasswordPolicyValidator
			extends AbstractPasswordPolicyValidator<RegistrationForm> {

		@Override
		protected char[] getPassword(RegistrationForm bean) {
			return bean.getPassword().toCharArray();
		}

		@Override
		protected void bindProperties(RegistrationForm bean, BiConsumer<String, Object> propertyBinder) {
			propertyBinder.accept( "username", bean.getUsername() );
		}
	}
	//end::classLevelValidator[]

	@Test
	public void classLevelPolicy() {
		//tag::classLevelUsage[]
		HibernateValidatorConfiguration configuration = Validation.byProvider( HibernateValidator.class )
				.configure();

		ConstraintMapping mapping = configuration.createConstraintMapping();
		mapping.constraintDefinition( PasswordPolicy.class )
				.validatedBy( RegistrationFormPasswordPolicyValidator.class );

		Validator validator = configuration
				.addMapping( mapping )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(
				new RegistrationForm( "john", "Str0ngP@ss" ) );
		assertNoViolations( violations );

		violations = validator.validate(
				new RegistrationForm( "john", "john1234A" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( PasswordPolicy.class )
						.withMessage( "password must not contain the username 'john'" )
		);
		//end::classLevelUsage[]
	}
}
