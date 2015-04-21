/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import javax.validation.Constraint;
import javax.validation.ConstraintTarget;
import javax.validation.ConstraintValidator;
import javax.validation.ValidationException;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;

import org.hibernate.validator.constraints.ConstraintComposition;
import org.hibernate.validator.constraints.EAN;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.LuhnCheck;
import org.hibernate.validator.constraints.Mod10Check;
import org.hibernate.validator.constraints.Mod11Check;
import org.hibernate.validator.constraints.ModCheck;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;
import org.hibernate.validator.internal.constraintvalidators.bv.AssertFalseValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.AssertTrueValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.DecimalMaxValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.DecimalMaxValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.DecimalMinValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.DecimalMinValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.DigitsValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.DigitsValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.hv.EANValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForCalendar;
import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForChronoZonedDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForDate;
import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForInstant;
import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForOffsetDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForReadableInstant;
import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForReadablePartial;
import org.hibernate.validator.internal.constraintvalidators.hv.LengthValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.LuhnCheckValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.MaxValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.MaxValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.MinValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.MinValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.hv.Mod10CheckValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.Mod11CheckValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.ModCheckValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.NotBlankValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.NullValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.ParameterScriptAssertValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForCalendar;
import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForChronoZonedDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForDate;
import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForInstant;
import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForOffsetDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForReadableInstant;
import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForReadablePartial;
import org.hibernate.validator.internal.constraintvalidators.bv.PatternValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.SafeHtmlValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.ScriptAssertValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArray;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfBoolean;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfByte;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfChar;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfInt;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfLong;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForCollection;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForMap;
import org.hibernate.validator.internal.constraintvalidators.hv.URLValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.br.CNPJValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.Version;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetAnnotationParameter;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethods;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newConcurrentHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Keeps track of builtin constraints and their validator implementations, as well as already resolved validator definitions.
 *
 * @author Hardy Ferentschik
 * @author Alaa Nassef
 * @author Gunnar Morling
 */
public class ConstraintHelper {
	public static final String GROUPS = "groups";
	public static final String PAYLOAD = "payload";
	public static final String MESSAGE = "message";
	public static final String VALIDATION_APPLIES_TO = "validationAppliesTo";

	private static final Log log = LoggerFactory.make();
	private static final String JODA_TIME_CLASS_NAME = "org.joda.time.ReadableInstant";

	// immutable
	private final Map<Class<? extends Annotation>, List<? extends Class<?>>> builtinConstraints;

	private final ValidatorClassMap validatorClasses = new ValidatorClassMap();

	public ConstraintHelper() {
		Map<Class<? extends Annotation>, List<? extends Class<?>>> tmpConstraints = newHashMap();

		putConstraint( tmpConstraints, AssertFalse.class, AssertFalseValidator.class );
		putConstraint( tmpConstraints, AssertTrue.class, AssertTrueValidator.class );
		putConstraint( tmpConstraints, CNPJ.class, CNPJValidator.class );
		putConstraint( tmpConstraints, CPF.class, CPFValidator.class );

		putConstraints( tmpConstraints, DecimalMax.class, DecimalMaxValidatorForNumber.class, DecimalMaxValidatorForCharSequence.class );
		putConstraints( tmpConstraints, DecimalMin.class, DecimalMinValidatorForNumber.class, DecimalMinValidatorForCharSequence.class );
		putConstraints( tmpConstraints, Digits.class, DigitsValidatorForCharSequence.class, DigitsValidatorForNumber.class );

		List<Class<? extends ConstraintValidator<Future, ?>>> futureValidators = newArrayList( 11 );
		futureValidators.add( FutureValidatorForCalendar.class );
		futureValidators.add( FutureValidatorForDate.class );
		if ( isJodaTimeInClasspath() ) {
			futureValidators.add( FutureValidatorForReadableInstant.class );
			futureValidators.add( FutureValidatorForReadablePartial.class );
		}
		if ( Version.getJavaRelease() >= 8 ) {
			// Java 8 date/time API validators
			futureValidators.add( FutureValidatorForChronoZonedDateTime.class );
			futureValidators.add( FutureValidatorForInstant.class );
			futureValidators.add( FutureValidatorForOffsetDateTime.class );
		}
		putConstraints( tmpConstraints, Future.class, futureValidators );

		putConstraints( tmpConstraints, Max.class, MaxValidatorForNumber.class, MaxValidatorForCharSequence.class );
		putConstraints( tmpConstraints, Min.class, MinValidatorForNumber.class, MinValidatorForCharSequence.class );
		putConstraint( tmpConstraints, NotNull.class, NotNullValidator.class );
		putConstraint( tmpConstraints, Null.class, NullValidator.class );

		List<Class<? extends ConstraintValidator<Past, ?>>> pastValidators = newArrayList( 11 );
		pastValidators.add( PastValidatorForCalendar.class );
		pastValidators.add( PastValidatorForDate.class );
		if ( isJodaTimeInClasspath() ) {
			pastValidators.add( PastValidatorForReadableInstant.class );
			pastValidators.add( PastValidatorForReadablePartial.class );
		}
		if ( Version.getJavaRelease() >= 8 ) {
			// Java 8 date/time API validators
			pastValidators.add( PastValidatorForChronoZonedDateTime.class );
			pastValidators.add( PastValidatorForInstant.class );
			pastValidators.add( PastValidatorForOffsetDateTime.class );
		}
		putConstraints( tmpConstraints, Past.class, pastValidators );

		putConstraint( tmpConstraints, Pattern.class, PatternValidator.class );

		List<Class<? extends ConstraintValidator<Size, ?>>> sizeValidators = newArrayList( 11 );
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

		putConstraints( tmpConstraints, Size.class, sizeValidators );

		putConstraint( tmpConstraints, EAN.class, EANValidator.class );
		putConstraint( tmpConstraints, Email.class, EmailValidator.class );
		putConstraint( tmpConstraints, Length.class, LengthValidator.class );
		putConstraint( tmpConstraints, ModCheck.class, ModCheckValidator.class );
		putConstraint( tmpConstraints, LuhnCheck.class, LuhnCheckValidator.class );
		putConstraint( tmpConstraints, Mod10Check.class, Mod10CheckValidator.class );
		putConstraint( tmpConstraints, Mod11Check.class, Mod11CheckValidator.class );
		putConstraint( tmpConstraints, NotBlank.class, NotBlankValidator.class );
		putConstraint( tmpConstraints, ParameterScriptAssert.class, ParameterScriptAssertValidator.class );
		putConstraint( tmpConstraints, SafeHtml.class, SafeHtmlValidator.class );
		putConstraint( tmpConstraints, ScriptAssert.class, ScriptAssertValidator.class );
		putConstraint( tmpConstraints, URL.class, URLValidator.class );

		this.builtinConstraints = Collections.unmodifiableMap( tmpConstraints );
	}

	private static <A extends Annotation> void putConstraint(Map<Class<? extends Annotation>, List<? extends Class<?>>> validators, Class<A> constraintType, Class<? extends ConstraintValidator<A, ?>> validatorType) {
		validators.put( constraintType, Collections.singletonList( validatorType ) );
	}

	private static <A extends Annotation> void putConstraints(Map<Class<? extends Annotation>, List<? extends Class<?>>> validators, Class<A> constraintType, Class<? extends ConstraintValidator<A, ?>> validatorType1, Class<? extends ConstraintValidator<A, ?>> validatorType2) {
		validators.put( constraintType, Collections.unmodifiableList( Arrays.<Class<?>>asList( validatorType1, validatorType2 ) ) );
	}

	private static <A extends Annotation> void putConstraints(Map<Class<? extends Annotation>, List<? extends Class<?>>> validators, Class<A> constraintType, List<Class<? extends ConstraintValidator<A, ?>>> validatorTypes) {
		validators.put( constraintType, Collections.unmodifiableList( validatorTypes ) );
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
	 * <li>internally registered validators for built-in constraints and</li>
	 * <li>XML configuration.</li>
	 * </ul>
	 *
	 * The result is cached internally.
	 *
	 * @param annotationType The constraint annotation type.
	 * @param <A> the type of the annotation
	 *
	 * @return The validator classes for the given type.
	 */
	public <A extends Annotation> List<Class<? extends ConstraintValidator<A, ?>>> getAllValidatorClasses(Class<A> annotationType) {
		Contracts.assertNotNull( annotationType, MESSAGES.classCannotBeNull() );

		List<Class<? extends ConstraintValidator<A, ?>>> classes = validatorClasses.get( annotationType );

		if ( classes == null ) {
			classes = getDefaultValidatorClasses( annotationType );

			List<Class<? extends ConstraintValidator<A, ?>>> cachedValidatorClasses = validatorClasses.putIfAbsent(
					annotationType,
					classes
			);

			if ( cachedValidatorClasses != null ) {
				classes = cachedValidatorClasses;
			}
		}

		return Collections.unmodifiableList( classes );
	}

	/**
	 * Returns those validator classes for the given constraint annotation
	 * matching the given target.
	 *
	 * @param annotationType The annotation of interest.
	 * @param validationTarget The target, either annotated element or parameters.
	 * @param <A> the type of the annotation
	 *
	 * @return A list with matching validator classes.
	 */
	public <A extends Annotation> List<Class<? extends ConstraintValidator<A, ?>>> findValidatorClasses(Class<A> annotationType, ValidationTarget validationTarget) {
		List<Class<? extends ConstraintValidator<A, ?>>> validatorClasses = getAllValidatorClasses( annotationType );
		List<Class<? extends ConstraintValidator<A, ?>>> matchingValidatorClasses = newArrayList();

		for ( Class<? extends ConstraintValidator<A, ?>> validatorClass : validatorClasses ) {
			if ( supportsValidationTarget( validatorClass, validationTarget ) ) {
				matchingValidatorClasses.add( validatorClass );
			}
		}

		return matchingValidatorClasses;
	}

	private boolean supportsValidationTarget(Class<? extends ConstraintValidator<?, ?>> validatorClass, ValidationTarget target) {
		SupportedValidationTarget supportedTargetAnnotation = validatorClass.getAnnotation(
				SupportedValidationTarget.class
		);

		//by default constraints target the annotated element
		if ( supportedTargetAnnotation == null ) {
			return target == ValidationTarget.ANNOTATED_ELEMENT;
		}

		return Arrays.asList( supportedTargetAnnotation.value() ).contains( target );
	}

	/**
	 * Registers the given validator classes with the given constraint
	 * annotation type.
	 *
	 * @param annotationType The constraint annotation type
	 * @param definitionClasses The validators to register
	 * @param keepDefaultClasses Whether any default validators should be kept or not
	 * @param <A> the type of the annotation
	 */
	public <A extends Annotation> void putValidatorClasses(Class<A> annotationType,
														   List<Class<? extends ConstraintValidator<A, ?>>> definitionClasses,
														   boolean keepDefaultClasses) {
		if ( keepDefaultClasses ) {
			List<Class<? extends ConstraintValidator<A, ?>>> defaultValidators = getDefaultValidatorClasses(
					annotationType
			);
			for ( Class<? extends ConstraintValidator<A, ?>> defaultValidator : defaultValidators ) {
				definitionClasses.add( 0, defaultValidator );
			}
		}

		validatorClasses.put( annotationType, definitionClasses );
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
			if ( m.getName().startsWith( "valid" ) && !m.getName().equals( VALIDATION_APPLIES_TO ) ) {
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
		catch ( ClassCastException e ) {
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
		catch ( ClassCastException e ) {
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
		boolean hasGenericValidators = !findValidatorClasses(
				annotationType,
				ValidationTarget.ANNOTATED_ELEMENT
		).isEmpty();
		boolean hasCrossParameterValidator = !findValidatorClasses(
				annotationType,
				ValidationTarget.PARAMETERS
		).isEmpty();
		final Method method = run( GetMethod.action( annotationType, VALIDATION_APPLIES_TO ) );

		if ( hasGenericValidators && hasCrossParameterValidator ) {
			if ( method == null ) {
				throw log.getGenericAndCrossParameterConstraintDoesNotDefineValidationAppliesToParameterException(
						annotationType.getName()
				);
			}
			if ( method.getReturnType() != ConstraintTarget.class ) {
				throw log.getValidationAppliesToParameterMustHaveReturnTypeConstraintTargetException( annotationType.getName() );
			}
			ConstraintTarget defaultValue = (ConstraintTarget) method.getDefaultValue();
			if ( defaultValue != ConstraintTarget.IMPLICIT ) {
				throw log.getValidationAppliesToParameterMustHaveDefaultValueImplicitException( annotationType.getName() );
			}
		}
		else if ( method != null ) {
			throw log.getValidationAppliesToParameterMustNotBeDefinedForNonGenericAndCrossParameterConstraintException(
					annotationType.getName()
			);
		}
	}

	public boolean isConstraintComposition(Class<? extends Annotation> annotationType) {
		return annotationType == ConstraintComposition.class;
	}

	private static boolean isJodaTimeInClasspath() {
		return isClassPresent( JODA_TIME_CLASS_NAME );
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
	private <A extends Annotation> List<Class<? extends ConstraintValidator<A, ?>>> getDefaultValidatorClasses(Class<A> annotationType) {
		//safe cause all CV for a given annotation A are CV<A, ?>
		final List<Class<? extends ConstraintValidator<A, ?>>> builtInValidators = (List<Class<? extends ConstraintValidator<A, ?>>>) builtinConstraints
				.get( annotationType );

		if ( builtInValidators != null ) {
			return builtInValidators;
		}

		Class<? extends ConstraintValidator<A, ?>>[] validatedBy = (Class<? extends ConstraintValidator<A, ?>>[]) annotationType
				.getAnnotation( Constraint.class )
				.validatedBy();

		return Arrays.asList( validatedBy );
	}

	private static boolean isClassPresent(String className) {
		try {
			run( LoadClass.action( className, ConstraintHelper.class.getClassLoader() ) );
			return true;
		}
		catch ( ValidationException e ) {
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
	private static class ValidatorClassMap {

		private final ConcurrentMap<Class<? extends Annotation>, List<? extends Class<?>>> constraintValidatorClasses = newConcurrentHashMap();

		private <A extends Annotation> List<Class<? extends ConstraintValidator<A, ?>>> get(Class<A> annotationType) {
			return (List<Class<? extends ConstraintValidator<A, ?>>>) constraintValidatorClasses.get( annotationType );
		}

		private <A extends Annotation> void put(Class<A> annotationType, List<Class<? extends ConstraintValidator<A, ?>>> validatorClasses) {
			constraintValidatorClasses.put( annotationType, validatorClasses );
		}

		private <A extends Annotation> List<Class<? extends ConstraintValidator<A, ?>>> putIfAbsent(Class<A> annotationType, List<Class<? extends ConstraintValidator<A, ?>>> classes) {
			return (List<Class<? extends ConstraintValidator<A, ?>>>) constraintValidatorClasses.putIfAbsent(
					annotationType,
					classes
			);
		}
	}
}
