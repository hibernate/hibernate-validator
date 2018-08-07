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
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import javax.validation.executable.ValidateOnExecution;

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
import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.UniqueElements;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;
import org.hibernate.validator.constraints.br.TituloEleitoral;
import org.hibernate.validator.constraints.pl.NIP;
import org.hibernate.validator.constraints.pl.PESEL;
import org.hibernate.validator.constraints.pl.REGON;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.StringHelper;
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
 * @author Guillaume Smet
 */
public class ValidateableBeanFilter implements Predicate<Class<?>> {

	private static final Set<Class<? extends Annotation>> MATCHING_ANNOTATIONS = Collections.unmodifiableSet(
			CollectionHelper.asSet( Valid.class, Constraint.class, ValidateOnExecution.class )
	);

	// We don't need to be exhaustive here as we will look for @Constraint annotations in the annotations of an
	// annotation. It is just an optimization.
	private static final Set<Class<? extends Annotation>> BEAN_VALIDATION_CONSTRAINTS = Collections.unmodifiableSet(
			CollectionHelper.asSet( AssertFalse.class, AssertTrue.class, DecimalMax.class, DecimalMin.class,
					Digits.class, Email.class, Future.class, FutureOrPresent.class, Max.class, Min.class,
					Negative.class, NegativeOrZero.class, NotBlank.class, NotEmpty.class, NotNull.class, Null.class,
					Past.class, PastOrPresent.class, Pattern.class, Positive.class, PositiveOrZero.class, Size.class )
	);

	// We don't need to be exhaustive here as we will look for @Constraint annotations in the annotations of an
	// annotation. It is just an optimization.
	@SuppressWarnings("deprecation")
	private static final Set<Class<? extends Annotation>> HIBERNATE_VALIDATOR_CONSTRAINTS = Collections.unmodifiableSet(
			CollectionHelper.asSet( CodePointLength.class, CreditCardNumber.class, Currency.class, EAN.class,
					Email.class, ISBN.class, Length.class, LuhnCheck.class, Mod10Check.class, Mod11Check.class,
					ModCheck.class, NotBlank.class, NotEmpty.class, ParameterScriptAssert.class, Range.class,
					SafeHtml.class, ScriptAssert.class, UniqueElements.class, URL.class, DurationMax.class,
					DurationMin.class, CNPJ.class, CPF.class, TituloEleitoral.class, NIP.class, PESEL.class,
					REGON.class ) );

	@Override
	public boolean test(Class<?> type) {
		for ( Class<?> clazz : ClassHierarchyHelper.getHierarchy( type ) ) {
			if ( isJdkClass( clazz ) ) {
				continue;
			}

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
		AnnotatedType[] annotatedParameterTypes = executable.getAnnotatedParameterTypes();
		for ( AnnotatedType annotatedParameterType : annotatedParameterTypes ) {
			if ( hasMatchingAnnotation( annotatedParameterType, processedAnnotations ) ) {
				return true;
			}
		}
		// NOTE: this check looks to be redundant BUT without it, test on BeanWithCustomConstraintOnParameter
		// will fail as executable.getAnnotatedParameterTypes() on BeanWithCustomConstraintOnParameter#doDefault()
		// will not contain matching annotations
		if ( annotatedParameterTypes.length > 0 ) {
			for ( Annotation[] annotations : executable.getParameterAnnotations() ) {
				if ( containsMatchingAnnotation( annotations, processedAnnotations ) ) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean containsMatchingAnnotation(Annotation[] annotations, Set<Annotation> processedAnnotations) {
		// we do a full pass on the current annotations
		for ( Annotation annotation : annotations ) {
			Class<? extends Annotation> annotationType = annotation.annotationType();

			if ( isJdkClass( annotationType ) ) {
				continue;
			}

			if ( MATCHING_ANNOTATIONS.contains( annotationType ) ||
					BEAN_VALIDATION_CONSTRAINTS.contains( annotationType ) ||
					HIBERNATE_VALIDATOR_CONSTRAINTS.contains( annotationType ) ) {
				return true;
			}
		}

		// and only after that we check the parent annotations
		for ( Annotation annotation : annotations ) {
			Class<? extends Annotation> annotationType = annotation.annotationType();

			if ( isJdkClass( annotationType ) ) {
				continue;
			}

			// Need to have this to prevent infinite loops, for example on annotations like @Target
			if ( !processedAnnotations.add( annotation ) ) {
				continue;
			}

			boolean containsMatchingAnnotation = containsMatchingAnnotation( annotationType.getDeclaredAnnotations(),
					processedAnnotations );
			if ( containsMatchingAnnotation ) {
				return true;
			}
		}

		return false;
	}

	private boolean isJdkClass(Class<?> clazz) {
		Package pakkage = clazz.getPackage();

		if ( pakkage == null || StringHelper.isNullOrEmptyString( pakkage.getName() ) ) {
			return false;
		}

		return ( pakkage.getName().startsWith( "java." ) || pakkage.getName().startsWith( "jdk.internal" ) );
	}
}
