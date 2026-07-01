/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter09;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.constraints.PasswordPolicy;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.spi.password.CharacterType;
import org.hibernate.validator.spi.password.PasswordPolicyBuilder;
import org.hibernate.validator.spi.password.PasswordPolicyDefinition;
import org.hibernate.validator.spi.password.PasswordPolicyDefinitionResolver;

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

		@Inject
		private DictionaryService dictionaryService;

		@Inject
		private PasswordHistoryService historyService;

		@Override
		public void configure(PasswordPolicyBuilder builder,
				HibernateConstraintValidatorInitializationContext context) {
			builder.minLength( 8 )
					.noSequence()
					.addRule( new DictionaryRule( dictionaryService ) )
					.addRule( new PasswordHistoryRule( historyService ) );
		}
	}
	//end::serviceRules[]

	public static class MyResolver implements PasswordPolicyDefinitionResolver {

		@Override
		public <T extends PasswordPolicyDefinition> T resolve(Class<T> definitionClass) {
			try {
				return definitionClass.getDeclaredConstructor().newInstance();
			}
			catch (ReflectiveOperationException e) {
				throw new RuntimeException( e );
			}
		}
	}

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

	@Test
	public void customResolver() {
		//tag::resolverRegistration[]
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValidationService( PasswordPolicyDefinitionResolver.class, new MyResolver() )
				.buildValidatorFactory()
				.getValidator();
		//end::resolverRegistration[]

		Set<ConstraintViolation<UserAccount>> violations = validator.validate(
				new UserAccount( "Passw0rd!" ) );
		assertNoViolations( violations );
	}

	private static class StrictPolicyBean {
		@PasswordPolicy(StrictPolicy.class)
		private final String password;

		StrictPolicyBean(String password) {
			this.password = password;
		}
	}
}
