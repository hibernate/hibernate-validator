/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.password;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.bean.BeanReference;
import org.hibernate.validator.bean.BeanRetrieval;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.PasswordPolicyDef;
import org.hibernate.validator.constraints.PasswordPolicy;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.spi.password.AbstractPasswordPolicyValidator;
import org.hibernate.validator.spi.password.CharacterType;
import org.hibernate.validator.spi.password.CompromisedPasswordChecker;
import org.hibernate.validator.spi.password.CompromisedPasswordResult;
import org.hibernate.validator.spi.password.KeyboardLayout;
import org.hibernate.validator.spi.password.PasswordContext;
import org.hibernate.validator.spi.password.PasswordPolicyBuilder;
import org.hibernate.validator.spi.password.PasswordPolicyDefinition;
import org.hibernate.validator.spi.password.PasswordPolicyDefinitionResolver;
import org.hibernate.validator.spi.password.PasswordPolicyRule;
import org.hibernate.validator.spi.password.PasswordStrengthEstimator;
import org.hibernate.validator.spi.password.PasswordStrengthResult;
import org.hibernate.validator.spi.password.PasswordStrengthScore;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PasswordPolicyValidatorTest {

	private Validator validator;

	@BeforeMethod
	public void setUp() {
		validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.getValidator();
	}

	@Test
	public void nullIsValid() {
		Set<ConstraintViolation<BasicPolicyBean>> violations = validator.validate( new BasicPolicyBean( null ) );
		assertNoViolations( violations );
	}

	@Test
	public void passwordMeetingAllRulesIsValid() {
		Set<ConstraintViolation<BasicPolicyBean>> violations = validator.validate(
				new BasicPolicyBean( "Passw0rd!xyz" ) );
		assertNoViolations( violations );
	}

	@Test
	public void passwordTooShortFails() {
		Set<ConstraintViolation<BasicPolicyBean>> violations = validator.validate(
				new BasicPolicyBean( "Aa1!" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );
	}

	@Test
	public void passwordMissingUppercaseFails() {
		Set<ConstraintViolation<BasicPolicyBean>> violations = validator.validate(
				new BasicPolicyBean( "password1!" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );
	}

	@Test
	public void passwordMissingDigitFails() {
		Set<ConstraintViolation<BasicPolicyBean>> violations = validator.validate(
				new BasicPolicyBean( "Password!x" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );
	}

	@Test
	public void passwordMissingLowercaseFails() {
		Set<ConstraintViolation<RequireLowercaseBean>> violations = validator.validate(
				new RequireLowercaseBean( "ALLUPPERCASE123" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		violations = validator.validate( new RequireLowercaseBean( "UPPERwithLower1" ) );
		assertNoViolations( violations );
	}

	@Test
	public void multipleViolationsReported() {
		Set<ConstraintViolation<BasicPolicyBean>> violations = validator.validate(
				new BasicPolicyBean( "ab" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( PasswordPolicy.class ),
				violationOf( PasswordPolicy.class ),
				violationOf( PasswordPolicy.class )
		);
	}

	@Test
	public void charArraySupport() {
		Set<ConstraintViolation<CharArrayPolicyBean>> violations = validator.validate(
				new CharArrayPolicyBean( null ) );
		assertNoViolations( violations );

		violations = validator.validate( new CharArrayPolicyBean( "Passw0rd!xyz".toCharArray() ) );
		assertNoViolations( violations );

		violations = validator.validate( new CharArrayPolicyBean( "ab".toCharArray() ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( PasswordPolicy.class ),
				violationOf( PasswordPolicy.class ),
				violationOf( PasswordPolicy.class )
		);
	}

	@Test
	public void noSequenceRule() {
		Set<ConstraintViolation<NoSequencePolicyBean>> violations = validator.validate(
				new NoSequencePolicyBean( "mypassabc" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		violations = validator.validate( new NoSequencePolicyBean( "my321pass" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		violations = validator.validate( new NoSequencePolicyBean( "myp@ssword" ) );
		assertNoViolations( violations );
	}

	@Test
	public void noKeyboardWalkRule() {
		// horizontal walk
		Set<ConstraintViolation<KeyboardWalkPolicyBean>> violations = validator.validate(
				new KeyboardWalkPolicyBean( "myqwertypass" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		// reverse walk
		violations = validator.validate( new KeyboardWalkPolicyBean( "ytrewqpass" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		// diagonal walk
		violations = validator.validate( new KeyboardWalkPolicyBean( "my4eszpass" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		// shifted characters: %=5, D=d, C=c, T=t
		violations = validator.validate( new KeyboardWalkPolicyBean( "%rDxCfT6" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		// walk too short (3 < default min 4)
		violations = validator.validate( new KeyboardWalkPolicyBean( "myasdpass" ) );
		assertNoViolations( violations );

		// no walk at all
		violations = validator.validate( new KeyboardWalkPolicyBean( "mzpxlqbk" ) );
		assertNoViolations( violations );
	}

	@Test
	public void noKeyboardWalkCustomLength() {
		Set<ConstraintViolation<KeyboardWalk3PolicyBean>> violations = validator.validate(
				new KeyboardWalk3PolicyBean( "myasdpass" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		violations = validator.validate( new KeyboardWalk3PolicyBean( "myqwpass" ) );
		assertNoViolations( violations );
	}

	@Test
	public void noKeyboardWalkAzerty() {
		// AZERTY home row walk: qsdf
		Set<ConstraintViolation<KeyboardWalkAzertyBean>> violations = validator.validate(
				new KeyboardWalkAzertyBean( "myqsdfpass" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		// AZERTY top row walk: azer
		violations = validator.validate( new KeyboardWalkAzertyBean( "myazerpass" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		// QWERTY-only walk (qwer) should NOT trigger on AZERTY-only policy
		violations = validator.validate( new KeyboardWalkAzertyBean( "myqwerpass" ) );
		assertNoViolations( violations );
	}

	@Test
	public void noKeyboardWalkCyrillicUa() {
		// Ukrainian Cyrillic top row walk: йцук
		Set<ConstraintViolation<KeyboardWalkCyrillicBean>> violations = validator.validate(
				new KeyboardWalkCyrillicBean( "паройцук" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		// Ukrainian Cyrillic home row walk: фіва
		violations = validator.validate( new KeyboardWalkCyrillicBean( "парофіва" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		// Latin chars should not trigger on Cyrillic-only policy
		violations = validator.validate( new KeyboardWalkCyrillicBean( "qwertypass" ) );
		assertNoViolations( violations );
	}

	@Test
	public void noKeyboardWalkMultiLayout() {
		// QWERTY walk should trigger
		Set<ConstraintViolation<KeyboardWalkMultiBean>> violations = validator.validate(
				new KeyboardWalkMultiBean( "myqwertypass" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		// AZERTY walk should also trigger
		violations = validator.validate( new KeyboardWalkMultiBean( "myazerpass" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		// no walk
		violations = validator.validate( new KeyboardWalkMultiBean( "mzpxlqbk" ) );
		assertNoViolations( violations );
	}

	@Test
	public void noKeyboardWalkCustomLayout() {
		// custom layout: "abcd" are adjacent (row 0, positions 0-3)
		Set<ConstraintViolation<KeyboardWalkCustomLayoutBean>> violations = validator.validate(
				new KeyboardWalkCustomLayoutBean( "xxabcdxx" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		// "qwer" are NOT adjacent on the custom layout
		violations = validator.validate( new KeyboardWalkCustomLayoutBean( "xxqwerxx" ) );
		assertNoViolations( violations );
	}

	@Test
	public void lambdaRule() {
		Set<ConstraintViolation<LambdaPolicyBean>> violations = validator.validate(
				new LambdaPolicyBean( "acmepassword" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		violations = validator.validate( new LambdaPolicyBean( "safepassword" ) );
		assertNoViolations( violations );
	}

	@Test
	public void customPasswordPolicyRule() {
		Set<ConstraintViolation<CustomRulePolicyBean>> violations = validator.validate(
				new CustomRulePolicyBean( "pwd" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		violations = validator.validate( new CustomRulePolicyBean( "longpassword" ) );
		assertNoViolations( violations );
	}

	@Test
	public void strengthEstimatorIntegration() {
		Validator v = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addBeanConfigurer( context -> context.define(
						PasswordStrengthEstimator.class,
						BeanReference.ofInstance( new StubStrengthEstimator() ) ) )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<StrengthPolicyBean>> violations = v.validate(
				new StrengthPolicyBean( "ab" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		violations = v.validate( new StrengthPolicyBean( "abcdefghijklmnop" ) );
		assertNoViolations( violations );
	}

	@Test
	public void compromisedCheckerIntegration() {
		Validator v = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addBeanConfigurer( context -> context.define(
						CompromisedPasswordChecker.class,
						BeanReference.ofInstance( new StubCompromisedChecker() ) ) )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<CompromisedPolicyBean>> violations = v.validate(
				new CompromisedPolicyBean( "compromised" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		violations = v.validate( new CompromisedPolicyBean( "safe" ) );
		assertNoViolations( violations );
	}

	@Test
	public void emptyStringIsValidated() {
		Set<ConstraintViolation<BasicPolicyBean>> violations = validator.validate( new BasicPolicyBean( "" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( PasswordPolicy.class ),
				violationOf( PasswordPolicy.class ),
				violationOf( PasswordPolicy.class )
		);
	}

	@Test
	public void repeatablePasswordPolicy() {
		Set<ConstraintViolation<RepeatablePolicyBean>> violations = validator.validate(
				new RepeatablePolicyBean( "Password1" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		violations = validator.validate( new RepeatablePolicyBean( "Password1!" ) );
		assertNoViolations( violations );
	}

	@Test
	public void customResolver() {
		TestResolver customResolver = new TestResolver();
		Validator v = Validation.byProvider( HibernateValidator.class )
				.configure()
				.passwordPolicyDefinitionResolver( customResolver )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<BasicPolicyBean>> violations = v.validate(
				new BasicPolicyBean( "Passw0rd!xyz" ) );
		assertNoViolations( violations );
	}

	@Test
	public void maxLengthRule() {
		Set<ConstraintViolation<MaxLengthPolicyBean>> violations = validator.validate(
				new MaxLengthPolicyBean( "a".repeat( 65 ) ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		violations = validator.validate( new MaxLengthPolicyBean( "a".repeat( 64 ) ) );
		assertNoViolations( violations );
	}

	@Test
	public void noWhitespaceRule() {
		Set<ConstraintViolation<NoWhitespacePolicyBean>> violations = validator.validate(
				new NoWhitespacePolicyBean( "pass word" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		violations = validator.validate( new NoWhitespacePolicyBean( "pass\tword" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		violations = validator.validate( new NoWhitespacePolicyBean( "password" ) );
		assertNoViolations( violations );
	}

	@Test
	public void noRepeatingCharactersRule() {
		Set<ConstraintViolation<NoRepeatingPolicyBean>> violations = validator.validate(
				new NoRepeatingPolicyBean( "paaassword" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		violations = validator.validate( new NoRepeatingPolicyBean( "paassword" ) );
		assertNoViolations( violations );

		violations = validator.validate( new NoRepeatingPolicyBean( "password" ) );
		assertNoViolations( violations );
	}

	@Test
	public void allowedCharactersRule() {
		Set<ConstraintViolation<AllowedCharsPolicyBean>> violations = validator.validate(
				new AllowedCharsPolicyBean( "abc123!" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		violations = validator.validate( new AllowedCharsPolicyBean( "abc123" ) );
		assertNoViolations( violations );
	}

	@Test
	public void illegalCharactersRule() {
		Set<ConstraintViolation<IllegalCharsPolicyBean>> violations = validator.validate(
				new IllegalCharsPolicyBean( "pass<word" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		violations = validator.validate( new IllegalCharsPolicyBean( "pass>word" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		violations = validator.validate( new IllegalCharsPolicyBean( "password" ) );
		assertNoViolations( violations );
	}

	@Test
	public void programmaticApi() {
		HibernateValidatorConfiguration config = Validation.byProvider( HibernateValidator.class ).configure();
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( ProgrammaticBean.class )
				.field( "password" )
				.constraint( new PasswordPolicyDef().value( BasicPolicy.class ) );

		Validator v = config.addMapping( mapping )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<ProgrammaticBean>> violations = v.validate(
				new ProgrammaticBean( "Passw0rd!xyz" ) );
		assertNoViolations( violations );

		violations = v.validate( new ProgrammaticBean( "ab" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( PasswordPolicy.class ),
				violationOf( PasswordPolicy.class ),
				violationOf( PasswordPolicy.class )
		);
	}

	@Test
	public void classLevelValidationWithAbstractPasswordPolicyValidator() {
		HibernateValidatorConfiguration config = Validation.byProvider( HibernateValidator.class ).configure();
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.constraintDefinition( PasswordPolicy.class )
				.validatedBy( UserRegistrationValidator.class );

		Validator v = config.addMapping( mapping )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<UserRegistration>> violations = v.validate(
				new UserRegistration( "admin", "myAdminPass1!" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( PasswordPolicy.class ) );

		violations = v.validate( new UserRegistration( "admin", "Str0ng!Pwd" ) );
		assertNoViolations( violations );
	}

	@Test
	public void classLevelValidationNullPasswordIsValid() {
		HibernateValidatorConfiguration config = Validation.byProvider( HibernateValidator.class ).configure();
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.constraintDefinition( PasswordPolicy.class )
				.validatedBy( UserRegistrationValidator.class );

		Validator v = config.addMapping( mapping )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<UserRegistration>> violations = v.validate(
				new UserRegistration( "admin", null ) );
		assertNoViolations( violations );
	}

	// --- Policy definitions ---

	public static class BasicPolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.minLength( 8 )
					.requireCharacters( CharacterType.UPPERCASE, 1 )
					.requireCharacters( CharacterType.DIGIT, 1 );
		}
	}

	public static class NoSequencePolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.noSequence();
		}
	}

	public static class KeyboardWalkPolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.noKeyboardWalk();
		}
	}

	public static class KeyboardWalk3Policy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.noKeyboardWalk( 3 );
		}
	}

	public static class KeyboardWalkAzertyPolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.noKeyboardWalk( KeyboardLayout.AZERTY );
		}
	}

	public static class KeyboardWalkCyrillicPolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.noKeyboardWalk( KeyboardLayout.CYRILLIC_UA );
		}
	}

	public static class KeyboardWalkMultiPolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.noKeyboardWalk( 4, KeyboardLayout.QWERTY, KeyboardLayout.AZERTY );
		}
	}

	public static class KeyboardWalkCustomLayoutPolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			KeyboardLayout custom = KeyboardLayout.of(
					new String[] { "aA", "bB", "cC", "dD", "eE", "fF" },
					new String[] { "gG", "hH", "iI", "jJ", "kK", "lL" },
					new String[] { "mM", "nN", "oO", "pP", "qQ", "rR" },
					new String[] { "sS", "tT", "uU", "vV", "wW", "xX" }
			);
			builder.noKeyboardWalk( custom );
		}
	}

	public static class LambdaPolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.addRule( "must not contain company name",
					pw -> !new String( pw ).toLowerCase().contains( "acme" ) );
		}
	}

	public static class CustomRulePolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.addRule( new MinTenCharsRule() );
		}
	}

	public static class StrengthPolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.strengthEstimator( PasswordStrengthScore.STRONG,
					context.getBeanResolver().resolve( PasswordStrengthEstimator.class, BeanRetrieval.ANY ).get() );
		}
	}

	public static class CompromisedPolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.compromisedChecker(
					context.getBeanResolver().resolve( CompromisedPasswordChecker.class, BeanRetrieval.ANY ).get() );
		}
	}

	public static class MaxLengthPolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.maxLength( 64 );
		}
	}

	public static class NoWhitespacePolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.noWhitespace();
		}
	}

	public static class NoRepeatingPolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.noRepeatingCharacters( 2 );
		}
	}

	public static class AllowedCharsPolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.allowedCharacters( 'a', 'b', 'c', '1', '2', '3' );
		}
	}

	public static class IllegalCharsPolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.illegalCharacters( '<', '>', '&' );
		}
	}

	public static class RequireSpecialPolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.requireCharacters( CharacterType.SPECIAL, 1 );
		}
	}

	public static class RequireLowercasePolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.requireCharacters( CharacterType.LOWERCASE, 1 );
		}
	}

	public static class NoUsernamePolicy implements PasswordPolicyDefinition {
		@Override
		public void configure(PasswordPolicyBuilder builder, HibernateConstraintValidatorInitializationContext context) {
			builder.minLength( 8 )
					.addRule( new NoUsernameRule() );
		}
	}

	// --- Beans ---

	private static class BasicPolicyBean {
		@PasswordPolicy(BasicPolicy.class)
		private final String password;

		BasicPolicyBean(String password) {
			this.password = password;
		}
	}

	private static class CharArrayPolicyBean {
		@PasswordPolicy(BasicPolicy.class)
		private final char[] password;

		CharArrayPolicyBean(char[] password) {
			this.password = password;
		}
	}

	private static class KeyboardWalkPolicyBean {
		@PasswordPolicy(KeyboardWalkPolicy.class)
		private final String password;

		KeyboardWalkPolicyBean(String password) {
			this.password = password;
		}
	}

	private static class KeyboardWalk3PolicyBean {
		@PasswordPolicy(KeyboardWalk3Policy.class)
		private final String password;

		KeyboardWalk3PolicyBean(String password) {
			this.password = password;
		}
	}

	private static class KeyboardWalkAzertyBean {
		@PasswordPolicy(KeyboardWalkAzertyPolicy.class)
		private final String password;

		KeyboardWalkAzertyBean(String password) {
			this.password = password;
		}
	}

	private static class KeyboardWalkCyrillicBean {
		@PasswordPolicy(KeyboardWalkCyrillicPolicy.class)
		private final String password;

		KeyboardWalkCyrillicBean(String password) {
			this.password = password;
		}
	}

	private static class KeyboardWalkMultiBean {
		@PasswordPolicy(KeyboardWalkMultiPolicy.class)
		private final String password;

		KeyboardWalkMultiBean(String password) {
			this.password = password;
		}
	}

	private static class KeyboardWalkCustomLayoutBean {
		@PasswordPolicy(KeyboardWalkCustomLayoutPolicy.class)
		private final String password;

		KeyboardWalkCustomLayoutBean(String password) {
			this.password = password;
		}
	}

	private static class NoSequencePolicyBean {
		@PasswordPolicy(NoSequencePolicy.class)
		private final String password;

		NoSequencePolicyBean(String password) {
			this.password = password;
		}
	}

	private static class LambdaPolicyBean {
		@PasswordPolicy(LambdaPolicy.class)
		private final String password;

		LambdaPolicyBean(String password) {
			this.password = password;
		}
	}

	private static class CustomRulePolicyBean {
		@PasswordPolicy(CustomRulePolicy.class)
		private final String password;

		CustomRulePolicyBean(String password) {
			this.password = password;
		}
	}

	private static class StrengthPolicyBean {
		@PasswordPolicy(StrengthPolicy.class)
		private final String password;

		StrengthPolicyBean(String password) {
			this.password = password;
		}
	}

	private static class CompromisedPolicyBean {
		@PasswordPolicy(CompromisedPolicy.class)
		private final String password;

		CompromisedPolicyBean(String password) {
			this.password = password;
		}
	}

	private static class MaxLengthPolicyBean {
		@PasswordPolicy(MaxLengthPolicy.class)
		private final String password;

		MaxLengthPolicyBean(String password) {
			this.password = password;
		}
	}

	private static class NoWhitespacePolicyBean {
		@PasswordPolicy(NoWhitespacePolicy.class)
		private final String password;

		NoWhitespacePolicyBean(String password) {
			this.password = password;
		}
	}

	private static class NoRepeatingPolicyBean {
		@PasswordPolicy(NoRepeatingPolicy.class)
		private final String password;

		NoRepeatingPolicyBean(String password) {
			this.password = password;
		}
	}

	private static class AllowedCharsPolicyBean {
		@PasswordPolicy(AllowedCharsPolicy.class)
		private final String password;

		AllowedCharsPolicyBean(String password) {
			this.password = password;
		}
	}

	private static class IllegalCharsPolicyBean {
		@PasswordPolicy(IllegalCharsPolicy.class)
		private final String password;

		IllegalCharsPolicyBean(String password) {
			this.password = password;
		}
	}

	private static class RequireLowercaseBean {
		@PasswordPolicy(RequireLowercasePolicy.class)
		private final String password;

		RequireLowercaseBean(String password) {
			this.password = password;
		}
	}

	private static class RepeatablePolicyBean {
		@PasswordPolicy(BasicPolicy.class)
		@PasswordPolicy(RequireSpecialPolicy.class)
		private final String password;

		RepeatablePolicyBean(String password) {
			this.password = password;
		}
	}

	private static class ProgrammaticBean {
		private final String password;

		ProgrammaticBean(String password) {
			this.password = password;
		}
	}

	// --- Stubs ---

	private static class MinTenCharsRule implements PasswordPolicyRule {
		@Override
		public String getMessage() {
			return "password must be at least 10 characters";
		}

		@Override
		public boolean isValid(PasswordContext passwordContext, HibernateConstraintValidatorContext context) {
			return passwordContext.password().length >= 10;
		}
	}

	private static class StubStrengthEstimator implements PasswordStrengthEstimator {
		@Override
		public PasswordStrengthResult estimate(char[] password) {
			int score = password.length < 8 ? PasswordStrengthScore.WEAK : PasswordStrengthScore.VERY_STRONG;
			return new PasswordStrengthResult() {
				@Override
				public int score() {
					return score;
				}

				@Override
				public String feedback() {
					return null;
				}
			};
		}
	}

	private static class StubCompromisedChecker implements CompromisedPasswordChecker {
		@Override
		public CompromisedPasswordResult check(char[] password) {
			boolean compromised = new String( password ).equals( "compromised" );
			return new CompromisedPasswordResult() {
				@Override
				public boolean compromised() {
					return compromised;
				}

				@Override
				public int occurrences() {
					return compromised ? 42 : 0;
				}
			};
		}
	}

	@PasswordPolicy(NoUsernamePolicy.class)
	private static class UserRegistration {
		private final String username;
		private final String password;

		UserRegistration(String username, String password) {
			this.username = username;
			this.password = password;
		}
	}

	public static class UserRegistrationValidator
			extends AbstractPasswordPolicyValidator<UserRegistration> {

		@Override
		protected char[] getPassword(UserRegistration bean) {
			return bean.password != null ? bean.password.toCharArray() : null;
		}

		@Override
		protected void bindProperties(UserRegistration bean, java.util.function.BiConsumer<String, Object> propertyBinder) {
			propertyBinder.accept( "username", bean.username );
		}
	}

	private static class NoUsernameRule implements PasswordPolicyRule {
		@Override
		public String getMessage() {
			return "password must not contain the username";
		}

		@Override
		public boolean isValid(PasswordContext passwordContext, HibernateConstraintValidatorContext context) {
			String username = passwordContext.get( "username", String.class );
			if ( username == null ) {
				return true;
			}
			return !new String( passwordContext.password() ).toLowerCase()
					.contains( username.toLowerCase() );
		}
	}

	private static class TestResolver implements PasswordPolicyDefinitionResolver {

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
}
