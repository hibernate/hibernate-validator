/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.money.MonetaryAmount;

import org.hibernate.validator.PredefinedScopeHibernateValidator;
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
public class PredefinedScopeAllConstraintsTest {

	@Test
	public void testConstraints() throws NoSuchMethodException, SecurityException {
		testConstraint( AssertFalse.class, new AssertFalseBean() );
		testConstraint( AssertTrue.class, new AssertTrueBean() );
		testConstraint( DecimalMax.class, new DecimalMaxBean() );
		testConstraint( DecimalMin.class, new DecimalMinBean() );
		testConstraint( Digits.class, new DigitsBean() );
		testConstraint( Email.class, new EmailBean() );
		testConstraint( Future.class, new FutureBean() );
		testConstraint( FutureOrPresent.class, new FutureOrPresentBean() );
		testConstraint( Max.class, new MaxBean() );
		testConstraint( Min.class, new MinBean() );
		testConstraint( Negative.class, new NegativeBean() );
		testConstraint( NegativeOrZero.class, new NegativeOrZeroBean() );
		testConstraint( NotBlank.class, new NotBlankBean() );
		testConstraint( NotEmpty.class, new NotEmptyBean() );
		testConstraint( NotNull.class, new NotNullBean() );
		testConstraint( Null.class, new NullBean() );
		testConstraint( Past.class, new PastBean() );
		testConstraint( PastOrPresent.class, new PastOrPresentBean() );
		testConstraint( Pattern.class, new PatternBean() );
		testConstraint( Positive.class, new PositiveBean() );
		testConstraint( PositiveOrZero.class, new PositiveOrZeroBean() );
		testConstraint( Size.class, new SizeBean() );
		testConstraint( CreditCardNumber.class, new CreditCardNumberBean() );
		testConstraint( Currency.class, new CurrencyBean() );
		testConstraint( EAN.class, new EANBean() );
		testConstraint( org.hibernate.validator.constraints.Email.class, new HvEmailBean() );
		testConstraint( ISBN.class, new ISBNBean() );
		testConstraint( Length.class, new LengthBean() );
		testConstraint( CodePointLength.class, new CodePointLengthBean() );
		testConstraint( LuhnCheck.class, new LuhnCheckBean() );
		testConstraint( Mod10Check.class, new Mod10CheckBean() );
		testConstraint( Mod11Check.class, new Mod11CheckBean() );
		testConstraint( ModCheck.class, new ModCheckBean() );
		testConstraint( Normalized.class, new NormalizedBean() );
		testConstraint( org.hibernate.validator.constraints.NotBlank.class, new HvNotBlankBean() );
		testConstraint( org.hibernate.validator.constraints.NotEmpty.class, new HvNotEmptyBean() );
		testConstraint( Range.class, new RangeBean() );
		testConstraint( UniqueElements.class, new UniqueElementsBean() );
		testConstraint( URL.class, new URLBean() );
		testConstraint( CNPJ.class, new CNPJBean() );
		testConstraint( CPF.class, new CPFBean() );
		testConstraint( TituloEleitoral.class, new TituloEleitoralBean() );
		testConstraint( REGON.class, new REGONBean() );
		testConstraint( NIP.class, new NIPBean() );
		testConstraint( PESEL.class, new PESELBean() );
		testConstraint( INN.class, new INNBean() );
		testConstraint( DurationMax.class, new DurationMaxBean() );
		testConstraint( DurationMin.class, new DurationMinBean() );
		testConstraint( ScriptAssert.class, new ScriptAssertBean() );

		Set<ConstraintViolation<ParameterScriptAssertBean>> parameterScriptAssertBeanViolations = getValidator( ParameterScriptAssert.class,
				ParameterScriptAssertBean.class ).forExecutables().validateParameters(
						new ParameterScriptAssertBean(), ParameterScriptAssertBean.class.getDeclaredMethod( "doTest", boolean.class ), new Object[]{ false } );

		ConstraintViolationAssert.assertThat( parameterScriptAssertBeanViolations )
				.containsOnlyViolations(
						violationOf( ParameterScriptAssert.class ) );
	}

	private <T> void testConstraint(Class<? extends Annotation> constraint, T bean) {
		Set<ConstraintViolation<T>> violations = getValidator( constraint, bean.getClass() )
				.validate( bean );
		ConstraintViolationAssert.assertThat( violations )
				.containsOnlyViolations(
						violationOf( constraint ) );
	}

	private static Validator getValidator(Class<? extends Annotation> constraint, Class<?> beanClass) {
		return Validation.byProvider( PredefinedScopeHibernateValidator.class )
				.configure()
				.builtinConstraints( Collections.singleton( constraint.getName() ) )
				.initializeBeanMetaData( Collections.singleton( beanClass ) )
				.buildValidatorFactory()
				.getValidator();
	}

	private static class AssertFalseBean {

		@AssertFalse
		private boolean assertFalse = true;
	}

	private static class AssertTrueBean {

		@AssertTrue
		private boolean assertTrue = false;
	}

	private static class DecimalMaxBean {

		@DecimalMax("3")
		private double decimalMax = 4;
	}

	private static class DecimalMinBean {

		@DecimalMin("3")
		private double decimalMin = 2;
	}

	private static class DigitsBean {

		@Digits(integer = 1, fraction = 3)
		private BigDecimal digits = BigDecimal.valueOf( 13333.3333f );
	}

	private static class EmailBean {

		@Email
		private String email = "invalid";
	}

	private static class FutureBean {

		@Future
		private LocalDate future = LocalDate.of( 2010, 10, 4 );
	}

	private static class FutureOrPresentBean {

		@FutureOrPresent
		private LocalDate futureOrPresent = LocalDate.of( 2010, 10, 4 );
	}

	private static class MaxBean {

		@Max(4)
		private int max = 6;
	}

	private static class MinBean {

		@Min(4)
		private int min = 2;
	}

	private static class NegativeBean {

		@Negative
		private int negative = 4;
	}

	private static class NegativeOrZeroBean {

		@NegativeOrZero
		private int negativeOrZero = 4;
	}

	private static class NotBlankBean {

		@NotBlank
		private String notBlank = "";
	}

	private static class NotEmptyBean {

		@NotEmpty
		private List<String> notEmpty = Collections.emptyList();
	}

	private static class NotNullBean {

		@NotNull
		private String notNull = null;
	}

	private static class NullBean {

		@Null
		private String nullConstraint = "not null";
	}

	private static class PastBean {

		@Past
		private LocalDate past = LocalDate.of( 2890, 10, 4 );
	}

	private static class PastOrPresentBean {

		@PastOrPresent
		private LocalDate pastOrPresent = LocalDate.of( 2890, 10, 4 );
	}

	private static class PatternBean {

		@Pattern(regexp = "[0-9]+")
		private String pattern = "invalid";
	}

	private static class PositiveBean {

		@Positive
		private int positive = -4;
	}

	private static class PositiveOrZeroBean {

		@PositiveOrZero
		private int positiveOrZero = -4;
	}

	private static class SizeBean {

		@Size(min = 2, max = 4)
		private String size = "666666";
	}

	private static class CreditCardNumberBean {

		@CreditCardNumber
		private String creditCardNumber = "invalid";
	}

	private static class CurrencyBean {

		@Currency("EUR")
		private MonetaryAmount currency = Money.of( 1000f, "USD" );
	}

	private static class EANBean {

		@EAN
		private String ean = "invalid";
	}

	private static class HvEmailBean {

		@org.hibernate.validator.constraints.Email
		private String hvEmail = "invalid";
	}

	private static class ISBNBean {

		@ISBN
		private String isbn = "invalid";
	}

	private static class LengthBean {

		@Length(min = 2, max = 4)
		private String length = "666666";
	}

	private static class CodePointLengthBean {

		@CodePointLength(min = 2, max = 4)
		private String codePointLength = "666666";
	}

	private static class LuhnCheckBean {

		@LuhnCheck
		private String luhnCheck = "4";
	}

	private static class Mod10CheckBean {

		@Mod10Check
		private String mod10Check = "4";
	}

	private static class Mod11CheckBean {

		@Mod11Check
		private String mod11Check = "4";
	}

	private static class ModCheckBean {

		@ModCheck(multiplier = 2, modType = ModCheck.ModType.MOD10)
		private String modCheck = "4";
	}

	private static class NormalizedBean {

		@Normalized(form = java.text.Normalizer.Form.NFKC)
		private String normalized = "\uFE64script\uFE65";

	}

	private static class HvNotBlankBean {

		@org.hibernate.validator.constraints.NotBlank
		private String hvNotBlank = "";
	}

	private static class HvNotEmptyBean {

		@org.hibernate.validator.constraints.NotEmpty
		private List<String> hvNotEmpty = Collections.emptyList();
	}

	private static class RangeBean {

		@Range(min = 2, max = 4)
		private int range = 6;
	}

	private static class UniqueElementsBean {

		@UniqueElements
		private List<String> uniqueElements = Arrays.asList( "a", "a" );
	}

	private static class URLBean {

		@URL
		private String url = "invalid";
	}

	private static class CNPJBean {

		@CNPJ
		private String cnpj = "invalid";
	}

	private static class CPFBean {

		@CPF
		private String cpf = "invalid";
	}

	private static class TituloEleitoralBean {

		@TituloEleitoral
		private String tituloEleitoral = "invalid";
	}

	private static class REGONBean {

		@REGON
		private String regon = "invalid";
	}

	private static class NIPBean {

		@NIP
		private String nip = "invalid";
	}

	private static class PESELBean {

		@PESEL
		private String pesel = "invalid";
	}

	private static class INNBean {

		@INN
		private String inn = "invalid";
	}

	private static class DurationMaxBean {

		@DurationMax(days = 4, hours = 4, minutes = 4, millis = 4, nanos = 4)
		private Duration durationMax = Duration.ofDays( 8 );
	}

	private static class DurationMinBean {

		@DurationMin(days = 4, hours = 4, minutes = 4, millis = 4, nanos = 4)
		private Duration durationMin = Duration.ofDays( 2 );
	}

	@ScriptAssert(lang = "groovy", script = "_this.scriptAssert")
	private static class ScriptAssertBean {

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
