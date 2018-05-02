/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cdi.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
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
 * on a type level, or on any member, or any parameter of any member. Super classes and all implemented interfaces are
 * considered while looking for these annotations.
 * <p>
 * The annotation may be applied as a meta-annotation on any annotation considered.
 * <p>
 * This filter is required as {@link WithAnnotations} does not look for annotations into the class hierarchy.
 *
 * @author Marko Bekhta
 */
public class ValidateableBeanFilter implements Predicate<Class<?>> {

	private static final Set<Class<? extends Annotation>> MATCHING_ANNOTATIONS = Collections.unmodifiableSet(
			CollectionHelper.asSet( Valid.class, Constraint.class, ValidateOnExecution.class )
	);

	@Override
	public boolean test(Class<?> type) {
		for ( Class<?> clazz : ClassHierarchyHelper.getHierarchy( type ) ) {
			if ( hasMatchingAnnotation( clazz ) ) {
				return true;
			}
		}

		return false;
	}

	private boolean hasMatchingAnnotation(Class<?> clazz) {
		Set<Annotation> processedAnnotations = new HashSet<>();
		// 1. Is present on a type level:
		if ( containsMatchingAnnotation( clazz.getDeclaredAnnotations(), processedAnnotations ) ) {
			return true;
		}

		// 2. Or on a field level
		for ( Field field : clazz.getDeclaredFields() ) {
			if ( hasMatchingAnnotation( field.getAnnotatedType(), processedAnnotations ) ) {
				return true;
			}
			if ( containsMatchingAnnotation( field.getDeclaredAnnotations(), processedAnnotations ) ) {
				return true;
			}
		}
		// 3. Or on any executable
		// 3.1 Constructors
		for ( Constructor<?> constructor : clazz.getDeclaredConstructors() ) {
			if ( hasMatchingAnnotation( constructor, processedAnnotations ) ) {
				return true;
			}
		}

		// 3.2 Methods
		for ( Method method : clazz.getDeclaredMethods() ) {
			if ( hasMatchingAnnotation( method, processedAnnotations ) ) {
				return true;
			}
		}
		return false;
	}

	private boolean containsMatchingAnnotation(Annotation[] annotations, Set<Annotation> processedAnnotations) {
		for ( Annotation annotation : annotations ) {
			if ( isMatchingAnnotation( annotation, processedAnnotations ) ) {
				return true;
			}
		}
		return false;
	}

	private boolean hasMatchingAnnotation(AnnotatedType annotatedType, Set<Annotation> processedAnnotations) {
		if ( containsMatchingAnnotation( annotatedType.getDeclaredAnnotations(), processedAnnotations ) ) {
			return true;
		}
		if ( annotatedType instanceof AnnotatedParameterizedType ) {
			for ( AnnotatedType type : ( (AnnotatedParameterizedType) annotatedType ).getAnnotatedActualTypeArguments() ) {
				if ( hasMatchingAnnotation( type, processedAnnotations ) ) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasMatchingAnnotation(Executable executable, Set<Annotation> processedAnnotations) {
		// Check the executable itself first
		if ( containsMatchingAnnotation( executable.getDeclaredAnnotations(), processedAnnotations ) ) {
			return true;
		}
		// Check its return value
		if ( hasMatchingAnnotation( executable.getAnnotatedReturnType(), processedAnnotations ) ) {
			return true;
		}
		// Then check its parameters
		for ( AnnotatedType annotatedParameterType : executable.getAnnotatedParameterTypes() ) {
			if ( hasMatchingAnnotation( annotatedParameterType, processedAnnotations ) ) {
				return true;
			}
		}
		// NOTE: this check looks to be redundant BUT without it, test on BeanWithCustomConstraintOnParameter
		// will fail as executable.getAnnotatedParameterTypes() on BeanWithCustomConstraintOnParameter#doDefault()
		// will not contain matching annotations
		for ( Annotation[] annotations : executable.getParameterAnnotations() ) {
			if ( containsMatchingAnnotation( annotations, processedAnnotations ) ) {
				return true;
			}
		}
		return false;
	}

	private boolean isMatchingAnnotation(Annotation annotationToCheck, Set<Annotation> processedAnnotations) {
		// Need to have this to prevent infinite loops, for example on annotations like @Target
		if ( !processedAnnotations.add( annotationToCheck ) ) {
			return false;
		}
		Class<? extends Annotation> annotationType = annotationToCheck.annotationType();
		if ( MATCHING_ANNOTATIONS.contains( annotationType ) ) {
			return true;
		}
		for ( Annotation annotation : annotationType.getDeclaredAnnotations() ) {
			if ( isMatchingAnnotation( annotation, processedAnnotations ) ) {
				return true;
			}
		}
		return false;
	}
}
