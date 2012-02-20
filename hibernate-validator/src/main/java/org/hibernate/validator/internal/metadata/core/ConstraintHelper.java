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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.Constraint;
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

import org.hibernate.validator.constraints.ConstraintComposition;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.ModCheck;
import org.hibernate.validator.constraints.NotBlank;
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
import org.hibernate.validator.internal.constraintvalidators.EmailValidator;
import org.hibernate.validator.internal.constraintvalidators.FutureValidatorForCalendar;
import org.hibernate.validator.internal.constraintvalidators.FutureValidatorForDate;
import org.hibernate.validator.internal.constraintvalidators.FutureValidatorForReadableInstant;
import org.hibernate.validator.internal.constraintvalidators.FutureValidatorForReadablePartial;
import org.hibernate.validator.internal.constraintvalidators.LengthValidator;
import org.hibernate.validator.internal.constraintvalidators.MaxValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.MaxValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.MinValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.MinValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.ModCheckValidator;
import org.hibernate.validator.internal.constraintvalidators.NotBlankValidator;
import org.hibernate.validator.internal.constraintvalidators.NotNullValidator;
import org.hibernate.validator.internal.constraintvalidators.NullValidator;
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
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * Keeps track of builtin constraints and their validator implementations, as well as already resolved validator definitions.
 *
 * @author Hardy Ferentschik
 * @author Alaa Nassef
 * @author Gunnar Morling
 */
public class ConstraintHelper {

	private static final Log log = LoggerFactory.make();

	private static final String JODA_TIME_CLASS_NAME = "org.joda.time.ReadableInstant";

	private final ConcurrentHashMap<Class<? extends Annotation>, List<Class<? extends ConstraintValidator<?, ?>>>> builtinConstraints =
			new ConcurrentHashMap<Class<? extends Annotation>, List<Class<? extends ConstraintValidator<?, ?>>>>();

	private final ConcurrentHashMap<Class<? extends Annotation>, List<Class<? extends ConstraintValidator<? extends Annotation, ?>>>> constraintValidatorDefinitions =
			new ConcurrentHashMap<Class<? extends Annotation>, List<Class<? extends ConstraintValidator<? extends Annotation, ?>>>>();

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
		constraintList.add( EmailValidator.class );
		builtinConstraints.put( Email.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( LengthValidator.class );
		builtinConstraints.put( Length.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( ModCheckValidator.class );
		builtinConstraints.put( ModCheck.class, constraintList );

		constraintList = newArrayList();
		constraintList.add( NotBlankValidator.class );
		builtinConstraints.put( NotBlank.class, constraintList );

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

	public List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> getBuiltInConstraints(Class<? extends Annotation> annotationClass) {
		final List<Class<? extends ConstraintValidator<?, ?>>> builtInList = builtinConstraints.get( annotationClass );

		if ( builtInList == null || builtInList.size() == 0 ) {
			throw log.unableToFindAnnotationConstraints( annotationClass );
		}

		List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> constraints = newArrayList( builtInList.size() );
		for ( Class<? extends ConstraintValidator<?, ?>> validatorClass : builtInList ) {
			//safe cause all CV for a given annotation A are CV<A, ?>
			@SuppressWarnings("unchecked")
			Class<ConstraintValidator<? extends Annotation, ?>> safeValidatorClass = (Class<ConstraintValidator<? extends Annotation, ?>>) validatorClass;
			constraints.add( safeValidatorClass );
		}

		return constraints;
	}

	public boolean isBuiltinConstraint(Class<? extends Annotation> annotationType) {
		return builtinConstraints.containsKey( annotationType );
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
		final Method method = ReflectionHelper.getMethod( annotationType, "value" );
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
	 * Checks whether a given annotation is a multi value constraint and returns the contained constraints if so.
	 *
	 * @param annotation the annotation to check.
	 *
	 * @return A list of constraint annotations or the empty list if <code>annotation</code> is not a multi constraint
	 *         annotation.
	 */
	public <A extends Annotation> List<Annotation> getMultiValueConstraints(A annotation) {
		List<Annotation> annotationList = newArrayList();
		try {
			final Method method = ReflectionHelper.getMethod( annotation.getClass(), "value" );
			if ( method != null ) {
				Class<?> returnType = method.getReturnType();
				if ( returnType.isArray() && returnType.getComponentType().isAnnotation() ) {
					Annotation[] annotations = (Annotation[]) method.invoke( annotation );
					for ( Annotation a : annotations ) {
						Class<? extends Annotation> annotationType = a.annotationType();
						if ( isConstraintAnnotation( annotationType ) || isBuiltinConstraint( annotationType ) ) {
							annotationList.add( a );
						}
					}
				}
			}
		}
		catch ( IllegalAccessException iae ) {
			// ignore
		}
		catch ( InvocationTargetException ite ) {
			// ignore
		}
		return annotationList;
	}

	/**
	 * Checks whether the specified annotation is a valid constraint annotation. A constraint annotations has to
	 * fulfill the following conditions:
	 * <ul>
	 * <li>Has to contain a <code>ConstraintValidator</code> implementation.</li>
	 * <li>Defines a message parameter.</li>
	 * <li>Defines a group parameter.</li>
	 * <li>Defines a payload parameter.</li>
	 * </ul>
	 *
	 * @param annotationType The annotation type to test.
	 *
	 * @return <code>true</code> if the annotation fulfills the above condtions, <code>false</code> otherwise.
	 */
	public boolean isConstraintAnnotation(Class<? extends Annotation> annotationType) {
		Constraint constraint = annotationType.getAnnotation( Constraint.class );
		if ( constraint == null ) {
			return false;
		}

		assertMessageParameterExists( annotationType );
		assertGroupsParameterExists( annotationType );
		assertPayloadParameterExists( annotationType );
		assertNoParameterStartsWithValid( annotationType );

		return true;
	}

	private void assertNoParameterStartsWithValid(Class<? extends Annotation> annotationType) {
		final Method[] methods = ReflectionHelper.getDeclaredMethods( annotationType );
		for ( Method m : methods ) {
			if ( m.getName().startsWith( "valid" ) ) {
				throw log.constraintParametersCannotStartWithValid();
			}
		}
	}

	private void assertPayloadParameterExists(Class<? extends Annotation> annotationType) {
		try {
			final Method method = ReflectionHelper.getMethod( annotationType, "payload" );
			if ( method == null ) {
				throw log.constraintWithoutMandatoryParameter( "payload", annotationType.getName() );
			}
			Class<?>[] defaultPayload = (Class<?>[]) method.getDefaultValue();
			if ( defaultPayload.length != 0 ) {
				throw log.wrongDefaultValueForPayloadParameter( annotationType.getName() );
			}
		}
		catch ( ClassCastException e ) {
			throw log.wrongTypeForPayloadParameter( annotationType.getName(), e );
		}
	}

	private void assertGroupsParameterExists(Class<? extends Annotation> annotationType) {
		try {
			final Method method = ReflectionHelper.getMethod( annotationType, "groups" );
			if ( method == null ) {
				throw log.constraintWithoutMandatoryParameter( "groups", annotationType.getName() );
			}
			Class<?>[] defaultGroups = (Class<?>[]) method.getDefaultValue();
			if ( defaultGroups.length != 0 ) {
				throw log.wrongDefaultValueForGroupsParameter( annotationType.getName() );
			}
		}
		catch ( ClassCastException e ) {
			throw log.wrongTypeForGroupsParameter( annotationType.getName(), e );
		}
	}

	private void assertMessageParameterExists(Class<? extends Annotation> annotationType) {
		final Method method = ReflectionHelper.getMethod( annotationType, "message" );
		if ( method == null ) {
			throw log.constraintWithoutMandatoryParameter( "message", annotationType.getName() );
		}
		if ( method.getReturnType() != String.class ) {
			throw log.wrongTypeForMessageParameter( annotationType.getName() );
		}
	}

	public <T extends Annotation> List<Class<? extends ConstraintValidator<T, ?>>> getConstraintValidatorDefinition
			(Class<T> annotationClass) {

		Contracts.assertNotNull( annotationClass, log.classCannotBeNull() );

		final List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> list = constraintValidatorDefinitions.get(
				annotationClass
		);

		List<Class<? extends ConstraintValidator<T, ?>>> constraintsValidators = newArrayList( list.size() );

		for ( Class<? extends ConstraintValidator<?, ?>> validatorClass : list ) {
			//safe cause all CV for a given annotation A are CV<A, ?>
			@SuppressWarnings("unchecked")
			Class<ConstraintValidator<T, ?>> safeValidatorClass = (Class<ConstraintValidator<T, ?>>) validatorClass;
			constraintsValidators.add( safeValidatorClass );
		}

		return constraintsValidators;
	}

	public <A extends Annotation> void addConstraintValidatorDefinition(Class<A> annotationClass, List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> definitionClasses) {
		constraintValidatorDefinitions.putIfAbsent( annotationClass, definitionClasses );
	}

	public boolean containsConstraintValidatorDefinition(Class<? extends Annotation> annotationClass) {
		return constraintValidatorDefinitions.containsKey( annotationClass );
	}

	public boolean isConstraintComposition(Class<? extends Annotation> annotationType) {
		return annotationType == ConstraintComposition.class;
	}

	private boolean isJodaTimeInClasspath() {
		boolean isInClasspath;
		try {
			ReflectionHelper.loadClass( JODA_TIME_CLASS_NAME, this.getClass() );
			isInClasspath = true;
		}
		catch ( ValidationException e ) {
			isInClasspath = false;
		}
		return isInClasspath;
	}
}
