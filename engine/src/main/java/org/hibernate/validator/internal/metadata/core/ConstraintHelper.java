/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.metadata.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import org.hibernate.validator.internal.constraintvalidators.AssertFalseValidator;
import org.hibernate.validator.internal.constraintvalidators.AssertTrueValidator;
import org.hibernate.validator.internal.constraintvalidators.DecimalMaxValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.DecimalMaxValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.DecimalMinValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.DecimalMinValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.DigitsValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.DigitsValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.EANValidator;
import org.hibernate.validator.internal.constraintvalidators.EmailValidator;
import org.hibernate.validator.internal.constraintvalidators.FutureValidatorForCalendar;
import org.hibernate.validator.internal.constraintvalidators.FutureValidatorForDate;
import org.hibernate.validator.internal.constraintvalidators.FutureValidatorForReadableInstant;
import org.hibernate.validator.internal.constraintvalidators.FutureValidatorForReadablePartial;
import org.hibernate.validator.internal.constraintvalidators.LengthValidator;
import org.hibernate.validator.internal.constraintvalidators.LuhnCheckValidator;
import org.hibernate.validator.internal.constraintvalidators.MaxValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.MaxValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.MinValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.MinValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.Mod10CheckValidator;
import org.hibernate.validator.internal.constraintvalidators.Mod11CheckValidator;
import org.hibernate.validator.internal.constraintvalidators.ModCheckValidator;
import org.hibernate.validator.internal.constraintvalidators.NotBlankValidator;
import org.hibernate.validator.internal.constraintvalidators.NotNullValidator;
import org.hibernate.validator.internal.constraintvalidators.NullValidator;
import org.hibernate.validator.internal.constraintvalidators.ParameterScriptAssertValidator;
import org.hibernate.validator.internal.constraintvalidators.PastValidatorForCalendar;
import org.hibernate.validator.internal.constraintvalidators.PastValidatorForDate;
import org.hibernate.validator.internal.constraintvalidators.PastValidatorForReadableInstant;
import org.hibernate.validator.internal.constraintvalidators.PastValidatorForReadablePartial;
import org.hibernate.validator.internal.constraintvalidators.PatternValidator;
import org.hibernate.validator.internal.constraintvalidators.SafeHtmlValidator;
import org.hibernate.validator.internal.constraintvalidators.ScriptAssertValidator;
import org.hibernate.validator.internal.constraintvalidators.SizeValidatorForArray;
import org.hibernate.validator.internal.constraintvalidators.SizeValidatorForArraysOfBoolean;
import org.hibernate.validator.internal.constraintvalidators.SizeValidatorForArraysOfByte;
import org.hibernate.validator.internal.constraintvalidators.SizeValidatorForArraysOfChar;
import org.hibernate.validator.internal.constraintvalidators.SizeValidatorForArraysOfDouble;
import org.hibernate.validator.internal.constraintvalidators.SizeValidatorForArraysOfFloat;
import org.hibernate.validator.internal.constraintvalidators.SizeValidatorForArraysOfInt;
import org.hibernate.validator.internal.constraintvalidators.SizeValidatorForArraysOfLong;
import org.hibernate.validator.internal.constraintvalidators.SizeValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.SizeValidatorForCollection;
import org.hibernate.validator.internal.constraintvalidators.SizeValidatorForMap;
import org.hibernate.validator.internal.constraintvalidators.URLValidator;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetAnnotationParameter;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethods;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newConcurrentHashMap;
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

	private final ConcurrentMap<Class<? extends Annotation>, List<? extends Class<?>>> builtinConstraints = newConcurrentHashMap();
	private final ValidatorClassMap validatorClasses = new ValidatorClassMap();

	public ConstraintHelper() {
		List<Class<? extends ConstraintValidator<?, ?>>> constraintList = newArrayList();
		constraintList.add( AssertFalseValidator.class );
		builtinConstraints.put( AssertFalse.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( AssertTrueValidator.class );
		builtinConstraints.put( AssertTrue.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( DecimalMaxValidatorForNumber.class );
		constraintList.add( DecimalMaxValidatorForCharSequence.class );
		builtinConstraints.put( DecimalMax.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( DecimalMinValidatorForNumber.class );
		constraintList.add( DecimalMinValidatorForCharSequence.class );
		builtinConstraints.put( DecimalMin.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( DigitsValidatorForCharSequence.class );
		constraintList.add( DigitsValidatorForNumber.class );
		builtinConstraints.put( Digits.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( FutureValidatorForCalendar.class );
		constraintList.add( FutureValidatorForDate.class );
		if ( isJodaTimeInClasspath() ) {
			constraintList.add( FutureValidatorForReadableInstant.class );
			constraintList.add( FutureValidatorForReadablePartial.class );
		}
		builtinConstraints.put( Future.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( MaxValidatorForNumber.class );
		constraintList.add( MaxValidatorForCharSequence.class );
		builtinConstraints.put( Max.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( MinValidatorForNumber.class );
		constraintList.add( MinValidatorForCharSequence.class );
		builtinConstraints.put( Min.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( NotNullValidator.class );
		builtinConstraints.put( NotNull.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( NullValidator.class );
		builtinConstraints.put( Null.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( PastValidatorForCalendar.class );
		constraintList.add( PastValidatorForDate.class );
		if ( isJodaTimeInClasspath() ) {
			constraintList.add( PastValidatorForReadableInstant.class );
			constraintList.add( PastValidatorForReadablePartial.class );
		}
		builtinConstraints.put( Past.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( PatternValidator.class );
		builtinConstraints.put( Pattern.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( SizeValidatorForCharSequence.class );
		constraintList.add( SizeValidatorForCollection.class );
		constraintList.add( SizeValidatorForArray.class );
		constraintList.add( SizeValidatorForMap.class );
		constraintList.add( SizeValidatorForArraysOfBoolean.class );
		constraintList.add( SizeValidatorForArraysOfByte.class );
		constraintList.add( SizeValidatorForArraysOfChar.class );
		constraintList.add( SizeValidatorForArraysOfDouble.class );
		constraintList.add( SizeValidatorForArraysOfFloat.class );
		constraintList.add( SizeValidatorForArraysOfInt.class );
		constraintList.add( SizeValidatorForArraysOfLong.class );
		builtinConstraints.put( Size.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( EANValidator.class );
		builtinConstraints.put( EAN.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( EmailValidator.class );
		builtinConstraints.put( Email.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( LengthValidator.class );
		builtinConstraints.put( Length.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( ModCheckValidator.class );
		builtinConstraints.put( ModCheck.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( LuhnCheckValidator.class );
		builtinConstraints.put( LuhnCheck.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( Mod10CheckValidator.class );
		builtinConstraints.put( Mod10Check.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( Mod11CheckValidator.class );
		builtinConstraints.put( Mod11Check.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( NotBlankValidator.class );
		builtinConstraints.put( NotBlank.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( ParameterScriptAssertValidator.class );
		builtinConstraints.put( ParameterScriptAssert.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( SafeHtmlValidator.class );
		builtinConstraints.put( SafeHtml.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( ScriptAssertValidator.class );
		builtinConstraints.put( ScriptAssert.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( URLValidator.class );
		builtinConstraints.put( URL.class, constraintList );
	}

	private <A extends Annotation> List<Class<? extends ConstraintValidator<A, ?>>> getBuiltInConstraints(Class<A> annotationClass) {
		//safe cause all CV for a given annotation A are CV<A, ?>
		@SuppressWarnings("unchecked")
		final List<Class<? extends ConstraintValidator<A, ?>>> builtInList = (List<Class<? extends ConstraintValidator<A, ?>>>) builtinConstraints
				.get( annotationClass );

		if ( builtInList == null || builtInList.size() == 0 ) {
			throw log.getUnableToFindAnnotationConstraintsException( annotationClass );
		}

		return builtInList;
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
	private <A extends Annotation> List<Class<? extends ConstraintValidator<A, ?>>> getDefaultValidatorClasses(Class<A> annotationType) {
		if ( isBuiltinConstraint( annotationType ) ) {
			return getBuiltInConstraints( annotationType );
		}
		else {
			@SuppressWarnings("unchecked")
			Class<? extends ConstraintValidator<A, ?>>[] validatedBy = (Class<? extends ConstraintValidator<A, ?>>[]) annotationType
					.getAnnotation( Constraint.class )
					.validatedBy();
			return Arrays.asList( validatedBy );
		}
	}

	private static boolean isClassPresent(String className) {
		try {
			run( LoadClass.action( className, ConstraintHelper.class ) );
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
