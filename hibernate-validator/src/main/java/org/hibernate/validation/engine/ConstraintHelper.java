// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Constraint;
import javax.validation.ConstraintDefinitionException;
import javax.validation.ConstraintValidator;
import javax.validation.ValidationException;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validation.constraints.AssertFalseValidator;
import org.hibernate.validation.constraints.AssertTrueValidator;
import org.hibernate.validation.constraints.DigitsValidatorForNumber;
import org.hibernate.validation.constraints.DigitsValidatorForString;
import org.hibernate.validation.constraints.FutureValidatorForCalendar;
import org.hibernate.validation.constraints.FutureValidatorForDate;
import org.hibernate.validation.constraints.MaxValidatorForNumber;
import org.hibernate.validation.constraints.MaxValidatorForString;
import org.hibernate.validation.constraints.MinValidatorForNumber;
import org.hibernate.validation.constraints.MinValidatorForString;
import org.hibernate.validation.constraints.NotNullValidator;
import org.hibernate.validation.constraints.NullValidator;
import org.hibernate.validation.constraints.PastValidatorForCalendar;
import org.hibernate.validation.constraints.PastValidatorForDate;
import org.hibernate.validation.constraints.PatternValidator;
import org.hibernate.validation.constraints.SizeValidatorForArray;
import org.hibernate.validation.constraints.SizeValidatorForArraysOfBoolean;
import org.hibernate.validation.constraints.SizeValidatorForArraysOfByte;
import org.hibernate.validation.constraints.SizeValidatorForArraysOfChar;
import org.hibernate.validation.constraints.SizeValidatorForArraysOfDouble;
import org.hibernate.validation.constraints.SizeValidatorForArraysOfFloat;
import org.hibernate.validation.constraints.SizeValidatorForArraysOfInt;
import org.hibernate.validation.constraints.SizeValidatorForArraysOfLong;
import org.hibernate.validation.constraints.SizeValidatorForCollection;
import org.hibernate.validation.constraints.SizeValidatorForMap;
import org.hibernate.validation.constraints.SizeValidatorForString;
import org.hibernate.validation.util.ReflectionHelper;

/**
 * Keeps track of builtin constraints and their validator implementations, as well as already resolved validator definitions.
 *
 * @author Hardy Ferentschik
 * @author Alaa Nassef
 */
public class ConstraintHelper {

	private final Map<Class<? extends Annotation>, List<Class<? extends ConstraintValidator<?, ?>>>> builtinConstraints =
			new HashMap<Class<? extends Annotation>, List<Class<? extends ConstraintValidator<?, ?>>>>();

	private final Map<Class<? extends Annotation>, List<Class<? extends ConstraintValidator<? extends Annotation, ?>>>> constraintValidatorDefinitons =
			new HashMap<Class<? extends Annotation>, List<Class<? extends ConstraintValidator<? extends Annotation, ?>>>>();

	public ConstraintHelper() {

		List<Class<? extends ConstraintValidator<?, ?>>> constraintList =
				new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( AssertFalseValidator.class );
		builtinConstraints.put( AssertFalse.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( AssertTrueValidator.class );
		builtinConstraints.put( AssertTrue.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( FutureValidatorForCalendar.class );
		constraintList.add( FutureValidatorForDate.class );
		builtinConstraints.put( Future.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( MaxValidatorForNumber.class );
		constraintList.add( MaxValidatorForString.class );
		builtinConstraints.put( Max.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( MinValidatorForNumber.class );
		constraintList.add( MinValidatorForString.class );
		builtinConstraints.put( Min.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( NotNullValidator.class );
		builtinConstraints.put( NotNull.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( NullValidator.class );
		builtinConstraints.put( Null.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( PastValidatorForCalendar.class );
		constraintList.add( PastValidatorForDate.class );
		builtinConstraints.put( Past.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( DigitsValidatorForString.class );
		constraintList.add( DigitsValidatorForNumber.class );
		builtinConstraints.put( Digits.class, constraintList );

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( SizeValidatorForString.class );
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

		constraintList = new ArrayList<Class<? extends ConstraintValidator<?, ?>>>();
		constraintList.add( PatternValidator.class );
		builtinConstraints.put( Pattern.class, constraintList );
	}

	public List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> getBuiltInConstraints(Class<? extends Annotation> annotationType) {
		final List<Class<? extends ConstraintValidator<?, ?>>> builtInList = getBuiltInFromAnnotationType(
				annotationType
		);

		if ( builtInList == null || builtInList.size() == 0 ) {
			throw new ValidationException( "Unable to find constraints for  " + annotationType );
		}
		List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> constraints =
				new ArrayList<Class<? extends ConstraintValidator<? extends Annotation, ?>>>( builtInList.size() );
		for ( Class<? extends ConstraintValidator<?, ?>> validatorClass : builtInList ) {
			//safe cause all CV for a given annotation A are CV<A, ?>
			@SuppressWarnings("unchecked")
			Class<ConstraintValidator<? extends Annotation, ?>> safeValdiatorClass = ( Class<ConstraintValidator<? extends Annotation, ?>> ) validatorClass;
			constraints.add( safeValdiatorClass );
		}

		return constraints;
	}

	private List<Class<? extends ConstraintValidator<?, ?>>> getBuiltInFromAnnotationType(Class<? extends Annotation> annotationType) {
		return builtinConstraints.get( annotationType );
	}

	public boolean isBuiltinConstraint(Class<? extends Annotation> annotationType) {
		return builtinConstraints.containsKey( annotationType );
	}

	/**
	 * Checks whether a given annotation is a multi value constraint or not.
	 *
	 * @param annotation the annotation to check.
	 *
	 * @return <code>true</code> if the specified annotation is a multi value constraints, <code>false</code>
	 *         otherwise.
	 */
	public boolean isMultiValueConstraint(Annotation annotation) {
		boolean isMultiValueConstraint = false;
		try {
			Method m = annotation.getClass().getMethod( "value" );
			Class returnType = m.getReturnType();
			if ( returnType.isArray() && returnType.getComponentType().isAnnotation() ) {
				Annotation[] annotations = ( Annotation[] ) m.invoke( annotation );
				for ( Annotation a : annotations ) {
					if ( isConstraintAnnotation( a ) || isBuiltinConstraint( a.annotationType() ) ) {
						isMultiValueConstraint = true;
					}
					else {
						isMultiValueConstraint = false;
						break;
					}
				}
			}
		}
		catch ( NoSuchMethodException nsme ) {
			// ignore
		}
		catch ( IllegalAccessException iae ) {
			// ignore
		}
		catch ( InvocationTargetException ite ) {
			// ignore
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
		List<Annotation> annotationList = new ArrayList<Annotation>();
		try {
			Method m = annotation.getClass().getMethod( "value" );
			Class returnType = m.getReturnType();
			if ( returnType.isArray() && returnType.getComponentType().isAnnotation() ) {
				Annotation[] annotations = ( Annotation[] ) m.invoke( annotation );
				for ( Annotation a : annotations ) {
					if ( isConstraintAnnotation( a ) || isBuiltinConstraint( a.annotationType() ) ) {
						annotationList.add( a );
					}
				}
			}
		}
		catch ( NoSuchMethodException nsme ) {
			// ignore
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
	 * </ul>
	 *
	 * @param annotation The annotation to test.
	 *
	 * @return <code>true</code> if the annotation fulfills the above condtions, <code>false</code> otherwise.
	 */
	public boolean isConstraintAnnotation(Annotation annotation) {

		Constraint constraint = annotation.annotationType()
				.getAnnotation( Constraint.class );
		if ( constraint == null ) {
			return false;
		}

		try {
			ReflectionHelper.getAnnotationParameter( annotation, "message", String.class );
		}
		catch ( Exception e ) {
			String msg = annotation.annotationType().getName() + " contains Constraint annotation, but does " +
					"not contain a message parameter.";
			throw new ConstraintDefinitionException( msg );
		}

		try {
			Class<?>[] defaultGroups = ( Class<?>[] ) annotation.annotationType()
					.getMethod( "groups" )
					.getDefaultValue();
			if ( defaultGroups.length != 0 ) {
				String msg = annotation.annotationType()
						.getName() + " contains Constraint annotation, but the groups " +
						"paramter default value is not empty.";
				throw new ConstraintDefinitionException( msg );
			}
		}
		catch ( NoSuchMethodException nsme ) {
			String msg = annotation.annotationType().getName() + " contains Constraint annotation, but does " +
					"not contain a groups parameter.";
			throw new ConstraintDefinitionException( msg );
		}

		Method[] methods = annotation.getClass().getMethods();
		for ( Method m : methods ) {
			if ( m.getName().startsWith( "valid" ) ) {
				String msg = "Parameters starting with 'valid' are not allowed in a constraint.";
				throw new ConstraintDefinitionException( msg );
			}
		}
		return true;
	}

	public List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> getConstraintValidatorDefinition(Class<? extends Annotation> annotationClass) {
		if ( annotationClass == null ) {
			throw new IllegalArgumentException( "Class cannot be null" );
		}
		return constraintValidatorDefinitons.get( annotationClass );
	}

	public <A extends Annotation> void addConstraintValidatorDefinition(Class<A> annotationClass, List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> definitionClasses) {
		constraintValidatorDefinitons.put( annotationClass, definitionClasses );
	}

	public boolean containsConstraintValidatorDefinition(Class<? extends Annotation> annotationClass) {
		return constraintValidatorDefinitons.containsKey( annotationClass );
	}
}
