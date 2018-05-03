/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cdi.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import javax.enterprise.inject.spi.WithAnnotations;
import javax.validation.Constraint;
import javax.validation.Valid;
import javax.validation.executable.ValidateOnExecution;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;

/**
 * A filter that checks if the passed class has any of these annotations:
 * <ul>
 * <li>{@link Valid}</li>
 * <li>{@link Constraint}</li>
 * <li>{@link ValidateOnExecution}</li>
 * </ul>
 * <p>
 * on a type level, or on any member, or any parameter of any member. Super classes
 * and all implemetned interfaces are considered while looking for these annotations.
 * <p>
 * The annotation may be applied as a meta-annotation on any annotation considered.
 * <p>
 * This filter is needed as {@link WithAnnotations} does not look into class hierarchy
 * to look for annotations.
 *
 * @author Marko Bekhta
 */
public class ValidateableBeanFilter implements Predicate<Class<?>> {

	private static final Set<Class<? extends Annotation>> ALLOWED_ANNOTATIONS = Collections.unmodifiableSet(
			CollectionHelper.asSet( Valid.class, Constraint.class, ValidateOnExecution.class )
	);

	@Override
	public boolean test(Class<?> type) {
		for ( Class<?> clazz : ClassHierarchyHelper.getHierarchy( type ) ) {
			if ( isAnnotationPresentAnywhere( clazz ) ) {
				return true;
			}
		}

		return false;
	}

	private boolean isAnnotationPresentAnywhere(Class<?> clazz) {
		Set<Annotation> processedAnnotations = new HashSet<>();
		// 1. Is present on a type level:
		if ( isAnnotationPresentIn( clazz.getDeclaredAnnotations(), processedAnnotations ) ) {
			return true;
		}

		// 2. Or on a field level
		for ( Field field : clazz.getDeclaredFields() ) {
			if ( isAnnotationPresentIn( field.getDeclaredAnnotations(), processedAnnotations ) ) {
				return true;
			}
		}
		// 3. Or on any executable.
		// 3.1 Constructors:
		for ( Constructor<?> constructor : clazz.getDeclaredConstructors() ) {
			if ( isAnnotationPresentOn( constructor, processedAnnotations ) ) {
				return true;
			}
		}

		// 3.2 Constructors:
		for ( Method method : clazz.getDeclaredMethods() ) {
			if ( isAnnotationPresentOn( method, processedAnnotations ) ) {
				return true;
			}
		}
		return false;
	}

	private boolean isAnnotationPresentIn(Annotation[] annotations, Set<Annotation> processedAnnotations) {
		for ( Annotation annotation : annotations ) {
			if ( isAnnotationAllowed( annotation, processedAnnotations ) ) {
				return true;
			}
		}
		return false;
	}

	private boolean isAnnotationPresentOn(Executable executable, Set<Annotation> processedAnnotations) {
		// Check the executable itself first:
		if ( isAnnotationPresentIn( executable.getDeclaredAnnotations(), processedAnnotations ) ) {
			return true;
		}
		// Then check its parameters:
		for ( Annotation[] annotations : executable.getParameterAnnotations() ) {
			if ( isAnnotationPresentIn( annotations, processedAnnotations ) ) {
				return true;
			}
		}
		return false;
	}

	private boolean isAnnotationAllowed(Annotation annotationToCheck, Set<Annotation> processedAnnotations) {
		// Need to have this to prevent infinite loops, for example on annotations like @Target
		if ( !processedAnnotations.add( annotationToCheck ) ) {
			return false;
		}
		Class<? extends Annotation> annotationType = annotationToCheck.annotationType();
		if ( ALLOWED_ANNOTATIONS.contains( annotationType ) ) {
			return true;
		}
		for ( Annotation annotation : annotationType.getDeclaredAnnotations() ) {
			if ( isAnnotationAllowed( annotation, processedAnnotations ) ) {
				return true;
			}
		}
		return false;
	}
}
