/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.money.MonetaryAmount;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.constraints.CodePointLength;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.constraints.Currency;
import org.hibernate.validator.constraints.EAN;
import org.hibernate.validator.constraints.ISBN;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.LuhnCheck;
import org.hibernate.validator.constraints.Mod10Check;
import org.hibernate.validator.constraints.Mod11Check;
import org.hibernate.validator.constraints.ModCheck;
import org.hibernate.validator.constraints.Normalized;
import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.UniqueElements;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;
import org.hibernate.validator.constraints.br.TituloEleitoral;
import org.hibernate.validator.constraints.pl.NIP;
import org.hibernate.validator.constraints.pl.PESEL;
import org.hibernate.validator.constraints.pl.REGON;
import org.hibernate.validator.constraints.ru.INN;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.hibernate.validator.testutil.ConstraintViolationAssert;
import org.javamoney.moneta.Money;
import org.testng.annotations.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Test that all the messages of all the constraints are properly interpolated for all the supported locales.
 *
 * @author Guillaume Smet
 */
@SuppressWarnings("deprecation")
public class MessagePropertiesTest {

	private static final List<Locale> ALL_SUPPORTED_LOCALES = Arrays.asList(
			Locale.forLanguageTag( "ar" ),
			Locale.forLanguageTag( "cs" ),
			Locale.forLanguageTag( "da" ),
			Locale.forLanguageTag( "de" ),
			Locale.forLanguageTag( "en" ),
			Locale.forLanguageTag( "es" ),
			Locale.forLanguageTag( "fa" ),
			Locale.forLanguageTag( "fr" ),
			Locale.forLanguageTag( "hu" ),
			Locale.forLanguageTag( "it" ),
			Locale.forLanguageTag( "ja" ),
			Locale.forLanguageTag( "ko" ),
			Locale.forLanguageTag( "mn-MN" ),
			Locale.forLanguageTag( "nl" ),
			Locale.forLanguageTag( "pl" ),
			Locale.forLanguageTag( "pt-BR" ),
			Locale.forLanguageTag( "ro" ),
			Locale.forLanguageTag( "ru" ),
			Locale.forLanguageTag( "sk" ),
			Locale.forLanguageTag( "tr" ),
			Locale.forLanguageTag( "uk" ),
			Locale.forLanguageTag( "zh-CN" ),
			Locale.forLanguageTag( "zh-TW" ),
			Locale.forLanguageTag( "zh" )
	);

	@Test
	public void testMessageProperties() throws NoSuchMethodException, SecurityException {
		List<String> invalidMessages = new ArrayList<>();

		for ( Locale locale : ALL_SUPPORTED_LOCALES ) {
			Validator validator = Validation.byProvider( HibernateValidator.class )
					.configure()
					.defaultLocale( locale )
					.buildValidatorFactory()
					.getValidator();

			Set<ConstraintViolation<Bean>> violations = validator.validate( new Bean() );

			ConstraintViolationAssert.assertThat( violations )
					.containsOnlyViolations(
							violationOf( AssertFalse.class ),
							violationOf( AssertTrue.class ),
							violationOf( DecimalMax.class ),
							violationOf( DecimalMin.class ),
							violationOf( Digits.class ),
							violationOf( Email.class ),
							violationOf( Future.class ),
							violationOf( FutureOrPresent.class ),
							violationOf( Max.class ),
							violationOf( Min.class ),
							violationOf( Negative.class ),
							violationOf( NegativeOrZero.class ),
							violationOf( NotBlank.class ),
							violationOf( NotEmpty.class ),
							violationOf( NotNull.class ),
							violationOf( Null.class ),
							violationOf( Past.class ),
							violationOf( PastOrPresent.class ),
							violationOf( Pattern.class ),
							violationOf( Positive.class ),
							violationOf( PositiveOrZero.class ),
							violationOf( Size.class ),
							violationOf( CreditCardNumber.class ),
							violationOf( Currency.class ),
							violationOf( EAN.class ),
							violationOf( org.hibernate.validator.constraints.Email.class ),
							violationOf( ISBN.class ),
							violationOf( Length.class ),
							violationOf( CodePointLength.class ),
							violationOf( LuhnCheck.class ),
							violationOf( Mod10Check.class ),
							violationOf( Mod11Check.class ),
							violationOf( ModCheck.class ),
							violationOf( Normalized.class ),
							violationOf( org.hibernate.validator.constraints.NotBlank.class ),
							violationOf( org.hibernate.validator.constraints.NotEmpty.class ),
							violationOf( Range.class ),
							violationOf( UniqueElements.class ),
							violationOf( URL.class ),
							violationOf( CNPJ.class ),
							violationOf( CPF.class ),
							violationOf( TituloEleitoral.class ),
							violationOf( REGON.class ),
							violationOf( NIP.class ),
							violationOf( PESEL.class ),
							violationOf( INN.class ),
							violationOf( DurationMax.class ),
							violationOf( DurationMin.class ),
							violationOf( ScriptAssert.class )
					);

			collectInvalidMessages( locale, invalidMessages, violations );

			Set<ConstraintViolation<ParameterScriptAssertBean>> parameterScriptAssertBeanViolations = validator.forExecutables().validateParameters(
					new ParameterScriptAssertBean(), ParameterScriptAssertBean.class.getDeclaredMethod( "doTest", boolean.class ), new Object[]{ false } );

			ConstraintViolationAssert.assertThat( parameterScriptAssertBeanViolations )
					.containsOnlyViolations(
							violationOf( ParameterScriptAssert.class ) );

			collectInvalidMessages( locale, invalidMessages, parameterScriptAssertBeanViolations );
		}

		if ( !invalidMessages.isEmpty() ) {
			throw new IllegalStateException( "Some messages are invalid:\n\t- " + String.join( "\n\t- ", invalidMessages ) + "\n" );
		}
	}

	private void collectInvalidMessages(Locale locale, List<String> invalidMessages, Set<? extends ConstraintViolation<?>> violations) {
		for ( ConstraintViolation<?> violation : violations ) {
			if ( violation.getMessage().contains( "{" ) ) {
				invalidMessages.add(
						"Message for constraint " + violation.getConstraintDescriptor().getAnnotation().annotationType() + " and locale " + locale
								+ " contains a curly brace: " + violation.getMessage() );
			}
			if ( violation.getMessage().contains( "$" ) ) {
				invalidMessages.add(
						"Message for constraint " + violation.getConstraintDescriptor().getAnnotation().annotationType() + " and locale " + locale
								+ " contains a dollar sign: " + violation.getMessage() );
			}
		}
	}

	@ScriptAssert(lang = "groovy", script = "_this.scriptAssert")
	private static class Bean {

		@AssertFalse
		private boolean assertFalse = true;

		@AssertTrue
		private boolean assertTrue = false;

		@DecimalMax("3")
		private double decimalMax = 4;

		@DecimalMin("3")
		private double decimalMin = 2;

		@Digits(integer = 1, fraction = 3)
		private BigDecimal digits = BigDecimal.valueOf( 13333.3333f );

		@Email
		private String email = "invalid";

		@Future
		private LocalDate future = LocalDate.of( 2010, 10, 4 );

		@FutureOrPresent
		private LocalDate futureOrPresent = LocalDate.of( 2010, 10, 4 );

		@Max(4)
		private int max = 6;

		@Min(4)
		private int min = 2;

		@Negative
		private int negative = 4;

		@NegativeOrZero
		private int negativeOrZero = 4;

		@NotBlank
		private String notBlank = "";

		@NotEmpty
		private List<String> notEmpty = Collections.emptyList();

		@NotNull
		private String notNull = null;

		@Null
		private String nullConstraint = "not null";

		@Past
		private LocalDate past = LocalDate.of( 2890, 10, 4 );

		@PastOrPresent
		private LocalDate pastOrPresent = LocalDate.of( 2890, 10, 4 );

		@Pattern(regexp = "[0-9]+")
		private String pattern = "invalid";

		@Positive
		private int positive = -4;

		@PositiveOrZero
		private int positiveOrZero = -4;

		@Size(min = 2, max = 4)
		private String size = "666666";

		@CreditCardNumber
		private String creditCardNumber = "invalid";

		@Currency("EUR")
		private MonetaryAmount currency = Money.of( 1000f, "USD" );

		@EAN
		private String ean = "invalid";

		@org.hibernate.validator.constraints.Email
		private String hvEmail = "invalid";

		@ISBN
		private String isbn = "invalid";

		@Length(min = 2, max = 4)
		private String length = "666666";

		@CodePointLength(min = 2, max = 4)
		private String codePointLength = "666666";

		@LuhnCheck
		private String luhnCheck = "4";

		@Mod10Check
		private String mod10Check = "4";

		@Mod11Check
		private String mod11Check = "4";

		@ModCheck(multiplier = 2, modType = ModCheck.ModType.MOD10)
		private String modCheck = "4";

		@Normalized(form = java.text.Normalizer.Form.NFKC)
		private String normalized = "\uFE64script\uFE65";

		@org.hibernate.validator.constraints.NotBlank
		private String hvNotBlank = "";

		@org.hibernate.validator.constraints.NotEmpty
		private List<String> hvNotEmpty = Collections.emptyList();

		@Range(min = 2, max = 4)
		private int range = 6;

		@UniqueElements
		private List<String> uniqueElements = Arrays.asList( "a", "a" );

		@URL
		private String url = "invalid";

		@CNPJ
		private String cnpj = "invalid";

		@CPF
		private String cpf = "invalid";

		@TituloEleitoral
		private String tituloEleitoral = "invalid";

		@REGON
		private String regon = "invalid";

		@NIP
		private String nip = "invalid";

		@PESEL
		private String pesel = "invalid";

		@INN
		private String inn = "invalid";

		@DurationMax(days = 4, hours = 4, minutes = 4, millis = 4, nanos = 4)
		private Duration durationMax = Duration.ofDays( 8 );

		@DurationMin(days = 4, hours = 4, minutes = 4, millis = 4, nanos = 4)
		private Duration durationMin = Duration.ofDays( 2 );

		@SuppressWarnings("unused")
		private boolean scriptAssert = false;
	}

	private static class ParameterScriptAssertBean {

		@ParameterScriptAssert(lang = "groovy", script = "test")
		public boolean doTest(boolean test) {
			return test;
		}
	}
}
