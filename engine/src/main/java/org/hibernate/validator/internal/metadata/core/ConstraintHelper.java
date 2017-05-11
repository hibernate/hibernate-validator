/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.core;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Constraint;
import javax.validation.ConstraintTarget;
import javax.validation.ConstraintValidator;
import javax.validation.ValidationException;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import javax.validation.constraintvalidation.ValidationTarget;

import org.hibernate.validator.constraints.ConstraintComposition;
import org.hibernate.validator.constraints.Currency;
import org.hibernate.validator.constraints.EAN;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.LuhnCheck;
import org.hibernate.validator.constraints.Mod10Check;
import org.hibernate.validator.constraints.Mod11Check;
import org.hibernate.validator.constraints.ModCheck;
import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;
import org.hibernate.validator.constraints.pl.NIP;
import org.hibernate.validator.constraints.pl.PESEL;
import org.hibernate.validator.constraints.pl.REGON;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.hibernate.validator.internal.constraintvalidators.bv.AssertFalseValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.AssertTrueValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.DecimalMaxValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.DecimalMinValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.DigitsValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.DigitsValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.MaxValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.MinValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.NotBlankValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.NullValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.PatternValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.money.CurrencyValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.bv.money.DecimalMaxValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.bv.money.DecimalMinValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.bv.money.MaxValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.bv.money.MinValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.bv.money.NegativeValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.bv.money.PositiveValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArray;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArraysOfBoolean;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArraysOfByte;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArraysOfChar;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArraysOfDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArraysOfFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArraysOfInt;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArraysOfLong;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArraysOfShort;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForCollection;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForMap;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArray;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfBoolean;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfByte;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfChar;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfInt;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfLong;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfShort;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForCollection;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForMap;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForCalendar;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForHijrahDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForInstant;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForJapaneseDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForLocalDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForLocalDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForLocalTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForMinguoDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForMonthDay;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForOffsetDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForOffsetTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForReadableInstant;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForReadablePartial;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForThaiBuddhistDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForYear;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForYearMonth;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForZonedDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForCalendar;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForHijrahDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForInstant;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForJapaneseDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForLocalDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForLocalDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForLocalTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForMinguoDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForMonthDay;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForOffsetDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForOffsetTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForReadableInstant;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForReadablePartial;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForThaiBuddhistDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForYear;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForYearMonth;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForZonedDateTime;
import org.hibernate.validator.internal.constraintvalidators.hv.EANValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.LengthValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.LuhnCheckValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.Mod10CheckValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.Mod11CheckValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.ModCheckValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.ParameterScriptAssertValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.SafeHtmlValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.ScriptAssertValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.URLValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.br.CNPJValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.pl.NIPValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.pl.PESELValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.pl.REGONValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.time.DurationMaxValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.time.DurationMinValidator;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorDescriptor;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetAnnotationParameter;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethods;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;

/**
 * Keeps track of builtin constraints and their validator implementations, as well as already resolved validator definitions.
 *
 * @author Hardy Ferentschik
 * @author Alaa Nassef
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class ConstraintHelper {
	public static final String GROUPS = "groups";
	public static final String PAYLOAD = "payload";
	public static final String MESSAGE = "message";
	public static final String VALIDATION_APPLIES_TO = "validationAppliesTo";

	private static final List<String> SUPPORTED_VALID_METHODS = Arrays.asList( VALIDATION_APPLIES_TO );

	private static final Log log = LoggerFactory.make();
	private static final String JODA_TIME_CLASS_NAME = "org.joda.time.ReadableInstant";
	private static final String JAVA_MONEY_CLASS_NAME = "javax.money.MonetaryAmount";

	// immutable
	private final Map<Class<? extends Annotation>, List<? extends ConstraintValidatorDescriptor<?>>> builtinConstraints;

	private final ValidatorDescriptorMap validatorDescriptors = new ValidatorDescriptorMap();

	public ConstraintHelper() {
		Map<Class<? extends Annotation>, List<ConstraintValidatorDescriptor<?>>> tmpConstraints = new HashMap<>();

		// Bean Validation constraints

		putConstraint( tmpConstraints, AssertFalse.class, AssertFalseValidator.class );
		putConstraint( tmpConstraints, AssertTrue.class, AssertTrueValidator.class );

		if ( isJavaMoneyInClasspath() ) {
			putConstraints( tmpConstraints, DecimalMax.class,  Arrays.asList(
					DecimalMaxValidatorForBigDecimal.class,
					DecimalMaxValidatorForBigInteger.class,
					DecimalMaxValidatorForDouble.class,
					DecimalMaxValidatorForFloat.class,
					DecimalMaxValidatorForLong.class,
					DecimalMaxValidatorForNumber.class,
					DecimalMaxValidatorForCharSequence.class,
					DecimalMaxValidatorForMonetaryAmount.class
			) );
			putConstraints( tmpConstraints, DecimalMin.class, Arrays.asList(
					DecimalMinValidatorForBigDecimal.class,
					DecimalMinValidatorForBigInteger.class,
					DecimalMinValidatorForDouble.class,
					DecimalMinValidatorForFloat.class,
					DecimalMinValidatorForLong.class,
					DecimalMinValidatorForNumber.class,
					DecimalMinValidatorForCharSequence.class,
					DecimalMinValidatorForMonetaryAmount.class
			) );
		}
		else {
			putConstraints( tmpConstraints, DecimalMax.class, Arrays.asList(
					DecimalMaxValidatorForBigDecimal.class,
					DecimalMaxValidatorForBigInteger.class,
					DecimalMaxValidatorForDouble.class,
					DecimalMaxValidatorForFloat.class,
					DecimalMaxValidatorForLong.class,
					DecimalMaxValidatorForNumber.class,
					DecimalMaxValidatorForCharSequence.class
			) );
			putConstraints( tmpConstraints, DecimalMin.class, Arrays.asList(
					DecimalMinValidatorForBigDecimal.class,
					DecimalMinValidatorForBigInteger.class,
					DecimalMinValidatorForDouble.class,
					DecimalMinValidatorForFloat.class,
					DecimalMinValidatorForLong.class,
					DecimalMinValidatorForNumber.class,
					DecimalMinValidatorForCharSequence.class
			) );
		}

		putConstraints( tmpConstraints, Digits.class, DigitsValidatorForCharSequence.class, DigitsValidatorForNumber.class );
		putConstraint( tmpConstraints, Email.class, EmailValidator.class );

		List<Class<? extends ConstraintValidator<Future, ?>>> futureValidators = new ArrayList<>( 18 );
		futureValidators.add( FutureValidatorForCalendar.class );
		futureValidators.add( FutureValidatorForDate.class );
		if ( isJodaTimeInClasspath() ) {
			futureValidators.add( FutureValidatorForReadableInstant.class );
			futureValidators.add( FutureValidatorForReadablePartial.class );
		}
		// Java 8 date/time API validators
		futureValidators.add( FutureValidatorForHijrahDate.class );
		futureValidators.add( FutureValidatorForInstant.class );
		futureValidators.add( FutureValidatorForJapaneseDate.class );
		futureValidators.add( FutureValidatorForLocalDate.class );
		futureValidators.add( FutureValidatorForLocalDateTime.class );
		futureValidators.add( FutureValidatorForLocalTime.class );
		futureValidators.add( FutureValidatorForMinguoDate.class );
		futureValidators.add( FutureValidatorForMonthDay.class );
		futureValidators.add( FutureValidatorForOffsetDateTime.class );
		futureValidators.add( FutureValidatorForOffsetTime.class );
		futureValidators.add( FutureValidatorForThaiBuddhistDate.class );
		futureValidators.add( FutureValidatorForYear.class );
		futureValidators.add( FutureValidatorForYearMonth.class );
		futureValidators.add( FutureValidatorForZonedDateTime.class );

		putConstraints( tmpConstraints, Future.class, futureValidators );

		if ( isJavaMoneyInClasspath() ) {
			putConstraints( tmpConstraints, Max.class, Arrays.asList(
					MaxValidatorForBigDecimal.class,
					MaxValidatorForBigInteger.class,
					MaxValidatorForDouble.class,
					MaxValidatorForFloat.class,
					MaxValidatorForLong.class,
					MaxValidatorForNumber.class,
					MaxValidatorForCharSequence.class,
					MaxValidatorForMonetaryAmount.class
			) );
			putConstraints( tmpConstraints, Min.class, Arrays.asList(
					MinValidatorForBigDecimal.class,
					MinValidatorForBigInteger.class,
					MinValidatorForDouble.class,
					MinValidatorForFloat.class,
					MinValidatorForLong.class,
					MinValidatorForNumber.class,
					MinValidatorForCharSequence.class,
					MinValidatorForMonetaryAmount.class
			) );
		}
		else {
			putConstraints( tmpConstraints, Max.class, Arrays.asList(
					MaxValidatorForBigDecimal.class,
					MaxValidatorForBigInteger.class,
					MaxValidatorForDouble.class,
					MaxValidatorForFloat.class,
					MaxValidatorForLong.class,
					MaxValidatorForNumber.class,
					MaxValidatorForCharSequence.class
			) );
			putConstraints( tmpConstraints, Min.class, Arrays.asList(
					MinValidatorForBigDecimal.class,
					MinValidatorForBigInteger.class,
					MinValidatorForDouble.class,
					MinValidatorForFloat.class,
					MinValidatorForLong.class,
					MinValidatorForNumber.class,
					MinValidatorForCharSequence.class
			) );
		}

		if ( isJavaMoneyInClasspath() ) {
			putConstraints( tmpConstraints, Negative.class, Arrays.asList(
					NegativeValidatorForBigDecimal.class,
					NegativeValidatorForBigInteger.class,
					NegativeValidatorForDouble.class,
					NegativeValidatorForFloat.class,
					NegativeValidatorForLong.class,
					NegativeValidatorForInteger.class,
					NegativeValidatorForShort.class,
					NegativeValidatorForByte.class,
					NegativeValidatorForNumber.class,
					NegativeValidatorForMonetaryAmount.class ) );
		}
		else {
			putConstraints( tmpConstraints, Negative.class, Arrays.asList(
					NegativeValidatorForBigDecimal.class,
					NegativeValidatorForBigInteger.class,
					NegativeValidatorForDouble.class,
					NegativeValidatorForFloat.class,
					NegativeValidatorForLong.class,
					NegativeValidatorForInteger.class,
					NegativeValidatorForShort.class,
					NegativeValidatorForByte.class,
					NegativeValidatorForNumber.class
			) );
		}
		putConstraint( tmpConstraints, NotBlank.class, NotBlankValidator.class );

		List<Class<? extends ConstraintValidator<NotEmpty, ?>>> notEmptyValidators = new ArrayList<>( 11 );
		notEmptyValidators.add( NotEmptyValidatorForCharSequence.class );
		notEmptyValidators.add( NotEmptyValidatorForCollection.class );
		notEmptyValidators.add( NotEmptyValidatorForArray.class );
		notEmptyValidators.add( NotEmptyValidatorForMap.class );
		notEmptyValidators.add( NotEmptyValidatorForArraysOfBoolean.class );
		notEmptyValidators.add( NotEmptyValidatorForArraysOfByte.class );
		notEmptyValidators.add( NotEmptyValidatorForArraysOfChar.class );
		notEmptyValidators.add( NotEmptyValidatorForArraysOfDouble.class );
		notEmptyValidators.add( NotEmptyValidatorForArraysOfFloat.class );
		notEmptyValidators.add( NotEmptyValidatorForArraysOfInt.class );
		notEmptyValidators.add( NotEmptyValidatorForArraysOfLong.class );
		notEmptyValidators.add( NotEmptyValidatorForArraysOfShort.class );
		putConstraints( tmpConstraints, NotEmpty.class, notEmptyValidators );

		putConstraint( tmpConstraints, NotNull.class, NotNullValidator.class );
		putConstraint( tmpConstraints, Null.class, NullValidator.class );

		List<Class<? extends ConstraintValidator<Past, ?>>> pastValidators = new ArrayList<>( 18 );
		pastValidators.add( PastValidatorForCalendar.class );
		pastValidators.add( PastValidatorForDate.class );
		if ( isJodaTimeInClasspath() ) {
			pastValidators.add( PastValidatorForReadableInstant.class );
			pastValidators.add( PastValidatorForReadablePartial.class );
		}
		// Java 8 date/time API validators
		pastValidators.add( PastValidatorForHijrahDate.class );
		pastValidators.add( PastValidatorForInstant.class );
		pastValidators.add( PastValidatorForJapaneseDate.class );
		pastValidators.add( PastValidatorForLocalDate.class );
		pastValidators.add( PastValidatorForLocalDateTime.class );
		pastValidators.add( PastValidatorForLocalTime.class );
		pastValidators.add( PastValidatorForMinguoDate.class );
		pastValidators.add( PastValidatorForMonthDay.class );
		pastValidators.add( PastValidatorForOffsetDateTime.class );
		pastValidators.add( PastValidatorForOffsetTime.class );
		pastValidators.add( PastValidatorForThaiBuddhistDate.class );
		pastValidators.add( PastValidatorForYear.class );
		pastValidators.add( PastValidatorForYearMonth.class );
		pastValidators.add( PastValidatorForZonedDateTime.class );

		putConstraints( tmpConstraints, Past.class, pastValidators );

		putConstraint( tmpConstraints, Pattern.class, PatternValidator.class );
		if ( isJavaMoneyInClasspath() ) {
			putConstraints( tmpConstraints, Positive.class, Arrays.asList(
					PositiveValidatorForBigDecimal.class,
					PositiveValidatorForBigInteger.class,
					PositiveValidatorForDouble.class,
					PositiveValidatorForFloat.class,
					PositiveValidatorForLong.class,
					PositiveValidatorForInteger.class,
					PositiveValidatorForShort.class,
					PositiveValidatorForByte.class,
					PositiveValidatorForNumber.class,
					PositiveValidatorForMonetaryAmount.class ) );
		}
		else {
			putConstraints( tmpConstraints, Positive.class, Arrays.asList(
					PositiveValidatorForBigDecimal.class,
					PositiveValidatorForBigInteger.class,
					PositiveValidatorForDouble.class,
					PositiveValidatorForFloat.class,
					PositiveValidatorForLong.class,
					PositiveValidatorForInteger.class,
					PositiveValidatorForShort.class,
					PositiveValidatorForByte.class,
					PositiveValidatorForNumber.class
			) );
		}

		List<Class<? extends ConstraintValidator<Size, ?>>> sizeValidators = new ArrayList<>( 11 );
		sizeValidators.add( SizeValidatorForCharSequence.class );
		sizeValidators.add( SizeValidatorForCollection.class );
		sizeValidators.add( SizeValidatorForArray.class );
		sizeValidators.add( SizeValidatorForMap.class );
		sizeValidators.add( SizeValidatorForArraysOfBoolean.class );
		sizeValidators.add( SizeValidatorForArraysOfByte.class );
		sizeValidators.add( SizeValidatorForArraysOfChar.class );
		sizeValidators.add( SizeValidatorForArraysOfDouble.class );
		sizeValidators.add( SizeValidatorForArraysOfFloat.class );
		sizeValidators.add( SizeValidatorForArraysOfInt.class );
		sizeValidators.add( SizeValidatorForArraysOfLong.class );
		sizeValidators.add( SizeValidatorForArraysOfShort.class );
		putConstraints( tmpConstraints, Size.class, sizeValidators );

		// Hibernate Validator specific constraints

		putConstraint( tmpConstraints, CNPJ.class, CNPJValidator.class );
		putConstraint( tmpConstraints, CPF.class, CPFValidator.class );
		if ( isJavaMoneyInClasspath() ) {
			putConstraint( tmpConstraints, Currency.class, CurrencyValidatorForMonetaryAmount.class );
		}
		putConstraint( tmpConstraints, DurationMax.class, DurationMaxValidator.class );
		putConstraint( tmpConstraints, DurationMin.class, DurationMinValidator.class );
		putConstraint( tmpConstraints, EAN.class, EANValidator.class );
		putConstraint( tmpConstraints, org.hibernate.validator.constraints.Email.class, org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator.class );
		putConstraint( tmpConstraints, Length.class, LengthValidator.class );
		putConstraint( tmpConstraints, ModCheck.class, ModCheckValidator.class );
		putConstraint( tmpConstraints, LuhnCheck.class, LuhnCheckValidator.class );
		putConstraint( tmpConstraints, Mod10Check.class, Mod10CheckValidator.class );
		putConstraint( tmpConstraints, Mod11Check.class, Mod11CheckValidator.class );
		putConstraint( tmpConstraints, REGON.class, REGONValidator.class );
		putConstraint( tmpConstraints, NIP.class, NIPValidator.class );
		putConstraint( tmpConstraints, PESEL.class, PESELValidator.class );
		putConstraint( tmpConstraints, org.hibernate.validator.constraints.NotBlank.class, org.hibernate.validator.internal.constraintvalidators.hv.NotBlankValidator.class );
		putConstraint( tmpConstraints, ParameterScriptAssert.class, ParameterScriptAssertValidator.class );
		putConstraint( tmpConstraints, SafeHtml.class, SafeHtmlValidator.class );
		putConstraint( tmpConstraints, ScriptAssert.class, ScriptAssertValidator.class );
		putConstraint( tmpConstraints, URL.class, URLValidator.class );

		this.builtinConstraints = Collections.unmodifiableMap( tmpConstraints );
	}

	private static <A extends Annotation> void putConstraint(Map<Class<? extends Annotation>, List<ConstraintValidatorDescriptor<?>>> validators, Class<A> constraintType, Class<? extends ConstraintValidator<A, ?>> validatorType) {
		validators.put( constraintType, Collections.singletonList( ConstraintValidatorDescriptor.forClass( validatorType ) ) );
	}

	private static <A extends Annotation> void putConstraints(Map<Class<? extends Annotation>, List<ConstraintValidatorDescriptor<?>>> validators, Class<A> constraintType, Class<? extends ConstraintValidator<A, ?>> validatorType1, Class<? extends ConstraintValidator<A, ?>> validatorType2) {
		List<ConstraintValidatorDescriptor<?>> descriptors = Stream.of( validatorType1, validatorType2 )
				.map( ConstraintValidatorDescriptor::forClass )
				.collect( Collectors.toList() );

		validators.put( constraintType, CollectionHelper.toImmutableList( descriptors ) );
	}

	private static <A extends Annotation> void putConstraints(Map<Class<? extends Annotation>, List<ConstraintValidatorDescriptor<?>>> validators, Class<A> constraintType, List<Class<? extends ConstraintValidator<A, ?>>> validatorDescriptors) {
		List<ConstraintValidatorDescriptor<?>> descriptors = validatorDescriptors.stream()
				.map( ConstraintValidatorDescriptor::forClass )
				.collect( Collectors.toList() );

		validators.put( constraintType, CollectionHelper.toImmutableList( descriptors ) );
	}

	private boolean isBuiltinConstraint(Class<? extends Annotation> annotationType) {
		return builtinConstraints.containsKey( annotationType );
	}

	/**
	 * Returns the constraint validator classes for the given constraint
	 * annotation type, as retrieved from
	 *
	 * <ul>
	 * <li>{@link Constraint#validatedBy()},
	 * <li>internally registered validators for built-in constraints</li>
	 * <li>XML configuration and</li>
	 * <li>programmatically registered validators (see
	 * {@link org.hibernate.validator.cfg.ConstraintMapping#constraintDefinition(Class)}).</li>
	 * </ul>
	 *
	 * The result is cached internally.
	 *
	 * @param annotationType The constraint annotation type.
	 * @param <A> the type of the annotation
	 *
	 * @return The validator classes for the given type.
	 */
	public <A extends Annotation> List<ConstraintValidatorDescriptor<A>> getAllValidatorDescriptors(Class<A> annotationType) {
		Contracts.assertNotNull( annotationType, MESSAGES.classCannotBeNull() );
		return validatorDescriptors.computeIfAbsent( annotationType, a -> getDefaultValidatorDescriptors( a ) );
	}

	/**
	 * Returns those validator descriptors for the given constraint annotation
	 * matching the given target.
	 *
	 * @param annotationType The annotation of interest.
	 * @param validationTarget The target, either annotated element or parameters.
	 * @param <A> the type of the annotation
	 *
	 * @return A list with matching validator descriptors.
	 */
	public <A extends Annotation> List<ConstraintValidatorDescriptor<A>> findValidatorDescriptors(Class<A> annotationType, ValidationTarget validationTarget) {
		return getAllValidatorDescriptors( annotationType ).stream()
			.filter( d -> supportsValidationTarget( d, validationTarget ) )
			.collect( Collectors.toList() );
	}

	private boolean supportsValidationTarget(ConstraintValidatorDescriptor<?> validatorDescriptor, ValidationTarget target) {
		return validatorDescriptor.getValidationTargets().contains( target );
	}

	/**
	 * Registers the given validator descriptors with the given constraint
	 * annotation type.
	 *
	 * @param annotationType The constraint annotation type
	 * @param validatorDescriptors The validator descriptors to register
	 * @param keepExistingClasses Whether already-registered validators should be kept or not
	 * @param <A> the type of the annotation
	 */
	public <A extends Annotation> void putValidatorDescriptors(Class<A> annotationType,
														   List<ConstraintValidatorDescriptor<A>> validatorDescriptors,
														   boolean keepExistingClasses) {

		List<ConstraintValidatorDescriptor<A>> validatorDescriptorsToAdd = new ArrayList<>();

		if ( keepExistingClasses ) {
			List<ConstraintValidatorDescriptor<A>> existingvalidatorDescriptors = getAllValidatorDescriptors( annotationType );
			if ( existingvalidatorDescriptors != null ) {
				validatorDescriptorsToAdd.addAll( 0, existingvalidatorDescriptors );
			}
		}

		validatorDescriptorsToAdd.addAll( validatorDescriptors );

		this.validatorDescriptors.put( annotationType, CollectionHelper.toImmutableList( validatorDescriptorsToAdd ) );
	}

	/**
	 * Checks whether a given annotation is a multi value constraint or not.
	 *
	 * @param annotationType the annotation type to check.
	 *
	 * @return {@code true} if the specified annotation is a multi value constraints, {@code false}
	 *         otherwise.
	 */
	public boolean isMultiValueConstraint(Class<? extends Annotation> annotationType) {
		if ( isJdkAnnotation( annotationType ) ) {
			return false;
		}

		boolean isMultiValueConstraint = false;
		final Method method = run( GetMethod.action( annotationType, "value" ) );
		if ( method != null ) {
			Class<?> returnType = method.getReturnType();
			if ( returnType.isArray() && returnType.getComponentType().isAnnotation() ) {
				@SuppressWarnings("unchecked")
				Class<? extends Annotation> componentType = (Class<? extends Annotation>) returnType.getComponentType();
				if ( isConstraintAnnotation( componentType ) || isBuiltinConstraint( componentType ) ) {
					isMultiValueConstraint = true;
				}
				else {
					isMultiValueConstraint = false;
				}
			}
		}
		return isMultiValueConstraint;
	}

	/**
	 * Returns the constraints which are part of the given multi-value constraint.
	 * <p>
	 * Invoke {@link #isMultiValueConstraint(Class)} prior to calling this method to check whether a given constraint
	 * actually is a multi-value constraint.
	 *
	 * @param multiValueConstraint the multi-value constraint annotation from which to retrieve the contained constraints
	 * @param <A> the type of the annotation
	 *
	 * @return A list of constraint annotations, may be empty but never {@code null}.
	 */
	public <A extends Annotation> List<Annotation> getConstraintsFromMultiValueConstraint(A multiValueConstraint) {
		Annotation[] annotations = run(
				GetAnnotationParameter.action(
						multiValueConstraint,
						"value",
						Annotation[].class
				)
		);
		return Arrays.asList( annotations );
	}

	/**
	 * Checks whether the specified annotation is a valid constraint annotation. A constraint annotation has to
	 * fulfill the following conditions:
	 * <ul>
	 * <li>Must be annotated with {@link Constraint}
	 * <li>Define a message parameter</li>
	 * <li>Define a group parameter</li>
	 * <li>Define a payload parameter</li>
	 * </ul>
	 *
	 * @param annotationType The annotation type to test.
	 *
	 * @return {@code true} if the annotation fulfills the above conditions, {@code false} otherwise.
	 */
	public boolean isConstraintAnnotation(Class<? extends Annotation> annotationType) {
		if ( annotationType.getAnnotation( Constraint.class ) == null ) {
			return false;
		}

		assertMessageParameterExists( annotationType );
		assertGroupsParameterExists( annotationType );
		assertPayloadParameterExists( annotationType );
		assertValidationAppliesToParameterSetUpCorrectly( annotationType );
		assertNoParameterStartsWithValid( annotationType );

		return true;
	}

	private void assertNoParameterStartsWithValid(Class<? extends Annotation> annotationType) {
		final Method[] methods = run( GetDeclaredMethods.action( annotationType ) );
		for ( Method m : methods ) {
			if ( m.getName().startsWith( "valid" ) && !SUPPORTED_VALID_METHODS.contains( m.getName() ) ) {
				throw log.getConstraintParametersCannotStartWithValidException();
			}
		}
	}

	private void assertPayloadParameterExists(Class<? extends Annotation> annotationType) {
		try {
			final Method method = run( GetMethod.action( annotationType, PAYLOAD ) );
			if ( method == null ) {
				throw log.getConstraintWithoutMandatoryParameterException( PAYLOAD, annotationType.getName() );
			}
			Class<?>[] defaultPayload = (Class<?>[]) method.getDefaultValue();
			if ( defaultPayload.length != 0 ) {
				throw log.getWrongDefaultValueForPayloadParameterException( annotationType.getName() );
			}
		}
		catch (ClassCastException e) {
			throw log.getWrongTypeForPayloadParameterException( annotationType.getName(), e );
		}
	}

	private void assertGroupsParameterExists(Class<? extends Annotation> annotationType) {
		try {
			final Method method = run( GetMethod.action( annotationType, GROUPS ) );
			if ( method == null ) {
				throw log.getConstraintWithoutMandatoryParameterException( GROUPS, annotationType.getName() );
			}
			Class<?>[] defaultGroups = (Class<?>[]) method.getDefaultValue();
			if ( defaultGroups.length != 0 ) {
				throw log.getWrongDefaultValueForGroupsParameterException( annotationType.getName() );
			}
		}
		catch (ClassCastException e) {
			throw log.getWrongTypeForGroupsParameterException( annotationType.getName(), e );
		}
	}

	private void assertMessageParameterExists(Class<? extends Annotation> annotationType) {
		final Method method = run( GetMethod.action( annotationType, MESSAGE ) );
		if ( method == null ) {
			throw log.getConstraintWithoutMandatoryParameterException( MESSAGE, annotationType.getName() );
		}
		if ( method.getReturnType() != String.class ) {
			throw log.getWrongTypeForMessageParameterException( annotationType.getName() );
		}
	}

	private void assertValidationAppliesToParameterSetUpCorrectly(Class<? extends Annotation> annotationType) {
		boolean hasGenericValidators = !findValidatorDescriptors(
				annotationType,
				ValidationTarget.ANNOTATED_ELEMENT
		).isEmpty();
		boolean hasCrossParameterValidator = !findValidatorDescriptors(
				annotationType,
				ValidationTarget.PARAMETERS
		).isEmpty();
		final Method method = run( GetMethod.action( annotationType, VALIDATION_APPLIES_TO ) );

		if ( hasGenericValidators && hasCrossParameterValidator ) {
			if ( method == null ) {
				throw log.getGenericAndCrossParameterConstraintDoesNotDefineValidationAppliesToParameterException(
						annotationType
				);
			}
			if ( method.getReturnType() != ConstraintTarget.class ) {
				throw log.getValidationAppliesToParameterMustHaveReturnTypeConstraintTargetException( annotationType );
			}
			ConstraintTarget defaultValue = (ConstraintTarget) method.getDefaultValue();
			if ( defaultValue != ConstraintTarget.IMPLICIT ) {
				throw log.getValidationAppliesToParameterMustHaveDefaultValueImplicitException( annotationType );
			}
		}
		else if ( method != null ) {
			throw log.getValidationAppliesToParameterMustNotBeDefinedForNonGenericAndCrossParameterConstraintException(
					annotationType
			);
		}
	}

	public boolean isConstraintComposition(Class<? extends Annotation> annotationType) {
		return annotationType == ConstraintComposition.class;
	}

	public boolean isJdkAnnotation(Class<? extends Annotation> annotation) {
		Package pakkage = annotation.getPackage();
		return pakkage != null && pakkage.getName() != null &&
				( pakkage.getName().startsWith( "java." ) || pakkage.getName().startsWith( "jdk.internal" ) );
	}

	private static boolean isJodaTimeInClasspath() {
		return isClassPresent( JODA_TIME_CLASS_NAME );
	}

	private static boolean isJavaMoneyInClasspath() {
		return isClassPresent( JAVA_MONEY_CLASS_NAME );
	}

	/**
	 * Returns the default validators for the given constraint type.
	 *
	 * @param annotationType The constraint annotation type.
	 *
	 * @return A list with the default validators as retrieved from
	 *         {@link Constraint#validatedBy()} or the list of validators for
	 *         built-in constraints.
	 */
	@SuppressWarnings("unchecked")
	private <A extends Annotation> List<ConstraintValidatorDescriptor<A>> getDefaultValidatorDescriptors(Class<A> annotationType) {
		//safe cause all CV for a given annotation A are CV<A, ?>
		final List<ConstraintValidatorDescriptor<A>> builtInValidators = (List<ConstraintValidatorDescriptor<A>>) builtinConstraints
				.get( annotationType );

		if ( builtInValidators != null ) {
			return builtInValidators;
		}

		Class<? extends ConstraintValidator<A, ?>>[] validatedBy = (Class<? extends ConstraintValidator<A, ?>>[]) annotationType
				.getAnnotation( Constraint.class )
				.validatedBy();

		return Stream.of( validatedBy )
			.map( c -> ConstraintValidatorDescriptor.forClass( c ) )
			.collect( Collectors.collectingAndThen( Collectors.toList(), CollectionHelper::toImmutableList ) );
	}

	private static boolean isClassPresent(String className) {
		try {
			run( LoadClass.action( className, ConstraintHelper.class.getClassLoader() ) );
			return true;
		}
		catch (ValidationException e) {
			return false;
		}
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}

	/**
	 * A type-safe wrapper around a concurrent map from constraint types to
	 * associated validator classes. The casts are safe as data is added trough
	 * the typed API only.
	 *
	 * @author Gunnar Morling
	 */
	@SuppressWarnings("unchecked")
	private static class ValidatorDescriptorMap {

		private final ConcurrentMap<Class<? extends Annotation>, List<? extends ConstraintValidatorDescriptor<?>>> constraintValidatorDescriptors = new ConcurrentHashMap<>();

		private <A extends Annotation> void put(Class<A> annotationType, List<ConstraintValidatorDescriptor<A>> validatorDescriptors) {
			constraintValidatorDescriptors.put( annotationType, validatorDescriptors );
		}

		private <A extends Annotation> List<ConstraintValidatorDescriptor<A>> computeIfAbsent(Class<A> annotationType,
				Function<? super Class<A>, List<ConstraintValidatorDescriptor<A>>> mappingFunction) {
			return (List<ConstraintValidatorDescriptor<A>>) constraintValidatorDescriptors.computeIfAbsent(
					annotationType,
					(Function<? super Class<? extends Annotation>, ? extends List<? extends ConstraintValidatorDescriptor<?>>>) mappingFunction
			);
		}
	}
}
