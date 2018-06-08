/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.chrono.HijrahDate;
import java.time.chrono.JapaneseDate;
import java.time.chrono.MinguoDate;
import java.time.chrono.ThaiBuddhistDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor8;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import javax.lang.model.util.TypeKindVisitor8;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.internal.util.TypeNames.BeanValidationTypes;
import org.hibernate.validator.ap.internal.util.TypeNames.HibernateValidatorTypes;
import org.hibernate.validator.ap.internal.util.TypeNames.JavaMoneyTypes;
import org.hibernate.validator.ap.internal.util.TypeNames.JodaTypes;
import org.hibernate.validator.ap.internal.util.TypeNames.SupportedForUnwrapTypes;

/**
 * Helper class that deals with all constraint-related stuff, such as
 * determining whether a given annotation represents a constraint annotation or
 * whether a given annotation is allowed to be declared at a given element.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Guillaume Smet
 */
public class ConstraintHelper {

	/**
	 * Possible results of a constraint check as returned by
	 * {@link ConstraintHelper#checkConstraint(DeclaredType, TypeMirror)}.
	 *
	 * @author Gunnar Morling
	 */
	public enum ConstraintCheckResult {

		/**
		 * The checked constraint is allowed at the evaluated type.
		 */
		ALLOWED,

		/**
		 * The checked constraint is not allowed at the evaluated type.
		 */
		DISALLOWED,

		/**
		 * Multiple validators were found, that could validate the checked
		 * constrained at the evaluated type.
		 */
		MULTIPLE_VALIDATORS_FOUND
	}

	/**
	 * The type of an annotation with respect to the BV API.
	 *
	 * @author Gunnar Morling
	 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
	 */
	public enum AnnotationType {

		/**
		 * Given annotation is a constraint annotation (e.g. @Min).
		 */
		CONSTRAINT_ANNOTATION,

		/**
		 * Given annotation is a multi-valued annotation (e.g.
		 * {@code
		 * &#64;List({
		 * &#64;Min(10),
		 * &#64;Min(value=20, groups= Special.class})
		 * })
		 * }.
		 */
		MULTI_VALUED_CONSTRAINT_ANNOTATION,

		/**
		 * Given annotation is the {@code @Valid} annotation.
		 */
		GRAPH_VALIDATION_ANNOTATION,

		/**
		 * Given annotation is the @Constraint meta-annotation.
		 */
		CONSTRAINT_META_ANNOTATION,

		/**
		 * Given annotation is the @GroupSequence annotation.
		 */
		GROUP_SEQUENCE_ANNOTATION,

		/**
		 * Given annotation is the @GroupSequenceProvider annotation.
		 */
		GROUP_SEQUENCE_PROVIDER_ANNOTATION,

		/**
		 * Given annotation is not related to the BV API (e.g. @Resource).
		 */
		NO_CONSTRAINT_ANNOTATION
	}

	/**
	 * Defines the object on which a validation is targeted.
	 */
	public enum AnnotationProcessorValidationTarget {
		/**
		 * The validation targets the parameters of a method/constructor.
		 */
		PARAMETERS,

		/**
		 * The validation targets the value on which it is annotated or the return type of a method/constructor.
		 */
		ANNOTATED_ELEMENT
	}

	/**
	 * The validation target of a constraint annotation.
	 */
	public enum AnnotationProcessorConstraintTarget {
		/**
		 * Constraint applies to the parameters of a method or a constructor.
		 */
		PARAMETERS,

		/**
		 * Constraint applies to the return value of a method or a constructor.
		 */
		RETURN_VALUE,

		/**
		 * Discover the type when no ambiguity is present if neither on a method nor a constructor.
		 */
		IMPLICIT
	}

	/**
	 * {@code java.time} types supported by {@code @Past}, {@code @Future}, {@code @PastOrPresent} and {@code @FutureOrPresent} annotations.
	 */
	private static final Class<?>[] JAVA_TIME_TYPES_SUPPORTED_BY_FUTURE_AND_PAST_ANNOTATIONS = new Class<?>[] {
		HijrahDate.class,
		Instant.class,
		JapaneseDate.class,
		LocalDate.class,
		LocalDateTime.class,
		LocalTime.class,
		MinguoDate.class,
		MonthDay.class,
		OffsetDateTime.class,
		OffsetTime.class,
		ThaiBuddhistDate.class,
		Year.class,
		YearMonth.class,
		ZonedDateTime.class
	};

	/**
	 * Types supported by {@code @Size} and {@code @NotEmpty} annotations.
	 */
	private static final Class<?>[] TYPES_SUPPORTED_BY_SIZE_AND_NOT_EMPTY_ANNOTATIONS = new Class<?>[] {
		Object[].class,
		boolean[].class,
		byte[].class,
		char[].class,
		double[].class,
		float[].class,
		int[].class,
		long[].class,
		short[].class,
		Collection.class,
		Map.class,
		CharSequence.class
	};

	/**
	 * Contains the supported types for given constraints. Keyed by constraint
	 * annotation type names, each value is a set with the allowed types for the
	 * mapped constraint. The map is pre-populated with the supported
	 * constraints for the built-in constraints. Constraints for custom
	 * constraints are determined and stored on demand.
	 */
	private final Map<Name, Set<TypeMirror>> supportedTypesByConstraint;

	private final Map<Name, AnnotationType> annotationTypeCache;

	private final Map<Name, TypeMirror> supportedTypesUnwrappedByDefault;

	/**
	 * Caches composing constraints.
	 */
	private final Map<Name, Set<AnnotationMirror>> composingConstraintsByConstraints;

	private final Types typeUtils;

	private final AnnotationApiHelper annotationApiHelper;

	public ConstraintHelper(Types typeUtils, AnnotationApiHelper annotationApiHelper) {

		this.typeUtils = typeUtils;
		this.annotationApiHelper = annotationApiHelper;

		annotationTypeCache = CollectionHelper.newHashMap();
		supportedTypesByConstraint = CollectionHelper.newHashMap();
		composingConstraintsByConstraints = CollectionHelper.newHashMap();
		supportedTypesUnwrappedByDefault = CollectionHelper.newHashMap();

		//register BV-defined constraints
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.ASSERT_FALSE, Boolean.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.ASSERT_TRUE, Boolean.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.DECIMAL_MAX, Number.class, String.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.DECIMAL_MAX, JavaMoneyTypes.MONETARY_AMOUNT );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.DECIMAL_MIN, Number.class, String.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.DECIMAL_MIN, JavaMoneyTypes.MONETARY_AMOUNT );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.DIGITS, Number.class, String.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.EMAIL, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.FUTURE, Calendar.class, Date.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.FUTURE, JodaTypes.READABLE_PARTIAL, JodaTypes.READABLE_INSTANT );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.FUTURE, JAVA_TIME_TYPES_SUPPORTED_BY_FUTURE_AND_PAST_ANNOTATIONS );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.FUTURE_OR_PRESENT, Calendar.class, Date.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.FUTURE_OR_PRESENT, JodaTypes.READABLE_PARTIAL, JodaTypes.READABLE_INSTANT );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.FUTURE_OR_PRESENT, JAVA_TIME_TYPES_SUPPORTED_BY_FUTURE_AND_PAST_ANNOTATIONS );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.MAX, Number.class, String.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.MAX, JavaMoneyTypes.MONETARY_AMOUNT );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.MIN, Number.class, String.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.MIN, JavaMoneyTypes.MONETARY_AMOUNT );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.NEGATIVE, Number.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.NEGATIVE, JavaMoneyTypes.MONETARY_AMOUNT );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.NEGATIVE_OR_ZERO, Number.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.NEGATIVE_OR_ZERO, JavaMoneyTypes.MONETARY_AMOUNT );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.NOT_BLANK, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.NOT_EMPTY, TYPES_SUPPORTED_BY_SIZE_AND_NOT_EMPTY_ANNOTATIONS );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.NOT_NULL, Object.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.NULL, Object.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.PAST, Calendar.class, Date.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.PAST, JodaTypes.READABLE_PARTIAL, JodaTypes.READABLE_INSTANT );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.PAST, JAVA_TIME_TYPES_SUPPORTED_BY_FUTURE_AND_PAST_ANNOTATIONS );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.PAST_OR_PRESENT, Calendar.class, Date.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.PAST_OR_PRESENT, JodaTypes.READABLE_PARTIAL, JodaTypes.READABLE_INSTANT );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.PAST_OR_PRESENT, JAVA_TIME_TYPES_SUPPORTED_BY_FUTURE_AND_PAST_ANNOTATIONS );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.PATTERN, String.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.POSITIVE, Number.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.POSITIVE, JavaMoneyTypes.MONETARY_AMOUNT );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.POSITIVE_OR_ZERO, Number.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.POSITIVE_OR_ZERO, JavaMoneyTypes.MONETARY_AMOUNT );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.SIZE, TYPES_SUPPORTED_BY_SIZE_AND_NOT_EMPTY_ANNOTATIONS );

		//register HV-specific constraints
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.CODE_POINT_LENGTH, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.CURRENCY, JavaMoneyTypes.MONETARY_AMOUNT );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.DURATION_MAX, Duration.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.DURATION_MIN, Duration.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.EMAIL, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.ISBN, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.LENGTH, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.MOD_CHECK, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.LUHN_CHECK, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.MOD_10_CHECK, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.MOD_11_CHECK, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.REGON_CHECK, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.NIP_CHECK, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.PESEL_CHECK, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.NOT_BLANK, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.NOT_EMPTY, TYPES_SUPPORTED_BY_SIZE_AND_NOT_EMPTY_ANNOTATIONS );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.SAFE_HTML, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.SCRIPT_ASSERT, Object.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.UNIQUE_ELEMENTS, Collection.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.URL, CharSequence.class );

		registerSupportedTypesUnwrappedByDefault( SupportedForUnwrapTypes.OPTIONAL_INT, Integer.class.getName() );
		registerSupportedTypesUnwrappedByDefault( SupportedForUnwrapTypes.OPTIONAL_LONG, Long.class.getName() );
		registerSupportedTypesUnwrappedByDefault( SupportedForUnwrapTypes.OPTIONAL_DOUBLE, Double.class.getName() );
	}

	/**
	 * Checks whether the given type element represents a constraint annotation
	 * or not. That's the case, if the given element is annotated with the
	 * {@code @Constraint} meta-annotation (which is only allowed at annotation
	 * declarations).
	 *
	 * @param element The element of interest.
	 *
	 * @return True, if the given element is a constraint annotation type, false
	 *         otherwise.
	 */
	public boolean isConstraintAnnotation(Element element) {
		return annotationApiHelper.getMirror( element.getAnnotationMirrors(), BeanValidationTypes.CONSTRAINT ) != null;
	}

	/**
	 * Returns the {@link AnnotationType} of the given annotation.
	 *
	 * @param annotationMirror The annotation mirror of interest.
	 *
	 * @return The given mirror's annotation type.
	 */
	public AnnotationType getAnnotationType(AnnotationMirror annotationMirror) {

		Name key = getName( annotationMirror.getAnnotationType() );

		AnnotationType annotationType = annotationTypeCache.get( key );

		if ( annotationType != null ) {
			return annotationType;
		}

		if ( isConstraintAnnotation( annotationMirror ) ) {
			annotationType = AnnotationType.CONSTRAINT_ANNOTATION;
		}
		else if ( isMultiValuedConstraint( annotationMirror ) ) {
			annotationType = AnnotationType.MULTI_VALUED_CONSTRAINT_ANNOTATION;
		}
		else if ( isGraphValidationAnnotation( annotationMirror ) ) {
			annotationType = AnnotationType.GRAPH_VALIDATION_ANNOTATION;
		}
		else if ( isConstraintMetaAnnotation( annotationMirror ) ) {
			annotationType = AnnotationType.CONSTRAINT_META_ANNOTATION;
		}
		else if ( isGroupSequenceAnnotation( annotationMirror ) ) {
			annotationType = AnnotationType.GROUP_SEQUENCE_ANNOTATION;
		}
		else if ( isGroupSequenceProviderAnnotation( annotationMirror ) ) {
			annotationType = AnnotationType.GROUP_SEQUENCE_PROVIDER_ANNOTATION;
		}
		else {
			annotationType = AnnotationType.NO_CONSTRAINT_ANNOTATION;
		}

		annotationTypeCache.put( key, annotationType );

		return annotationType;
	}

	/**
	 * Returns a list with the constraint annotations contained in the given
	 * array-valued annotation mirror.
	 *
	 * @param annotationMirror An array-valued annotation mirror (meaning it has an
	 * array-typed attribute with name "value").
	 *
	 * @return A list with the constraint annotations part of the given
	 *         multi-valued constraint annotation. Will return an empty list if
	 *         the given annotation is no multi-valued annotation or if no
	 *         constraint annotations are contained within the given
	 *         array-valued annotation.
	 */
	public List<AnnotationMirror> getPartsOfMultiValuedConstraint(
			AnnotationMirror annotationMirror) {

		final List<AnnotationMirror> theValue = CollectionHelper.newArrayList();

		for ( AnnotationValue oneValuePart : annotationApiHelper
				.getAnnotationArrayValue( annotationMirror, "value" ) ) {

			oneValuePart.accept(
					new SimpleAnnotationValueVisitor8<Void, Void>() {

						@Override
						public Void visitAnnotation(AnnotationMirror a, Void p) {

							if ( isConstraintAnnotation(
									a.getAnnotationType()
											.asElement()
							) ) {
								theValue.add( a );
							}

							return null;
						}

					}, null
			);

		}

		return theValue;
	}

	/**
	 * Checks whether the given annotation type (which <b>must</b> be a
	 * constraint annotation type) may be specified at elements of the specified
	 * type.
	 *
	 * @param constraintAnnotationType A constraint annotation type.
	 * @param typeOfAnnotatedElement A type which with an element is annotated.
	 *
	 * @return Whether the given constraint annotation may be specified at
	 *         elements of the given type.
	 */
	public ConstraintCheckResult checkConstraint(DeclaredType constraintAnnotationType, TypeMirror typeOfAnnotatedElement) {

		//recursively check the composing constraints
		ConstraintCheckResult composingConstraintsCheck = checkComposingConstraints(
				constraintAnnotationType,
				typeOfAnnotatedElement
		);
		if ( composingConstraintsCheck != ConstraintCheckResult.ALLOWED ) {
			return composingConstraintsCheck;
		}

		//check the supported types of the constraint itself
		return checkSupportedTypes( constraintAnnotationType, typeOfAnnotatedElement );
	}

	/**
	 * Checks whether the given type element represents a composed constraint or not.
	 *
	 * @param element The type element of interest. Must not be null.
	 *
	 * @return True if the given element represents a composed constraint, false otherwise.
	 */
	public boolean isComposedConstraint(TypeElement element) {

		return Boolean.TRUE.equals(
				element.asType().accept(
						new TypeKindVisitor8<Boolean, Void>() {

							@Override
							public Boolean visitDeclared(DeclaredType constraintValidatorImplementation, Void p) {
								return !getComposingConstraints( constraintValidatorImplementation ).isEmpty();
							}

						}, null
				)
		);
	}

	/**
	 * Resolve the actual {@code AnnotationProcessorValidationTarget} of a constraint annotation, when applied to a method/constructor.
	 * <p>
	 * When the annotation supports multiple {@link AnnotationProcessorValidationTarget}s (i.e. it is both cross-parameter and generic), the actual target is resolved using the
	 * 'validationAppliesTo()' attribute of the annotation.
	 *
	 * @param element  the method/constructor on which the annotation is applied
	 * @param annotation  the constraint annotation
	 * @return the resolved {@code AnnotationProcessorValidationTarget}, null if the target cannot be inferred
	 */
	public AnnotationProcessorValidationTarget resolveValidationTarget(ExecutableElement element, AnnotationMirror annotation) {
		Set<AnnotationProcessorValidationTarget> allowedTargets = getSupportedValidationTargets( annotation.getAnnotationType() );

		// assume that at least one target (AnnotationProcessorValidationTarget.ANNOTATED_ELEMENT) is present

		if ( allowedTargets.size() == 1 ) {
			return allowedTargets.toArray( new AnnotationProcessorValidationTarget[1] )[0];
		}

		AnnotationProcessorConstraintTarget constrTarget = getConstraintTarget( annotation );
		if ( constrTarget == null ) {
			return null;
		}

		switch ( constrTarget ) {
			case PARAMETERS:
				return AnnotationProcessorValidationTarget.PARAMETERS;
			case RETURN_VALUE:
				return AnnotationProcessorValidationTarget.ANNOTATED_ELEMENT;
			case IMPLICIT:
			default:
				return resolveImplicitValidationTarget( element );
		}
	}

	/**
	 * Returns the set of {@code AnnotationProcessorValidationTarget} supported by the given constraint annotation type.
	 * <p>
	 * A constraint annotation can support {@link AnnotationProcessorValidationTarget#ANNOTATED_ELEMENT}, {@link AnnotationProcessorValidationTarget#PARAMETERS} or both.
	 *
	 * @param constraintAnnotationType  the constraint annotation type
	 * @return the set of supported {@code AnnotationProcessorValidationTarget}s
	 */
	public Set<AnnotationProcessorValidationTarget> getSupportedValidationTargets(DeclaredType constraintAnnotationType) {

		AnnotationMirror constraintMetaAnnotation = getConstraintMetaAnnotation( constraintAnnotationType );
		List<? extends AnnotationValue> validatorClassReferences = getValidatorClassesFromConstraintMetaAnnotation( constraintMetaAnnotation );

		EnumSet<AnnotationProcessorValidationTarget> supported = EnumSet.noneOf( AnnotationProcessorValidationTarget.class );

		for ( AnnotationValue oneValidatorClassReference : validatorClassReferences ) {
			supported.addAll( getSupportedValidationTargets( oneValidatorClassReference ) );
		}

		if ( supported.isEmpty() ) {
			// case of built-in validation constraints
			supported.add( AnnotationProcessorValidationTarget.ANNOTATED_ELEMENT );
		}

		return supported;
	}

	/**
	 * Check that a constraint has at most one cross-parameter validator that resolves to Object or Object[].
	 *
	 * @param constraintAnnotationType  the constraint type
	 * @return {@code ConstraintCheckResult#MULTIPLE_VALIDATORS_FOUND} if the constraint has more than one cross-parameter validator,
	 * 			{@code ConstraintCheckResult#DISALLOWED} if the constraint has one cross-parameter validator with a wrong generic type,
	 * 			{@code ConstraintCheckResult#ALLOWED} otherwise
	 */
	public ConstraintCheckResult checkCrossParameterTypes(DeclaredType constraintAnnotationType) {

		AnnotationMirror constraintMetaAnnotation = getConstraintMetaAnnotation( constraintAnnotationType );
		List<? extends AnnotationValue> validatorClassReferences = getValidatorClassesFromConstraintMetaAnnotation( constraintMetaAnnotation );

		AnnotationValue crossParameterValidator = null;
		for ( AnnotationValue oneValidatorClassReference : validatorClassReferences ) {
			Set<AnnotationProcessorValidationTarget> targets = getSupportedValidationTargets( oneValidatorClassReference );
			if ( targets.contains( AnnotationProcessorValidationTarget.PARAMETERS ) ) {
				if ( crossParameterValidator != null ) {
					return ConstraintCheckResult.MULTIPLE_VALIDATORS_FOUND;
				}
				crossParameterValidator = oneValidatorClassReference;
			}
		}

		if ( crossParameterValidator != null ) {

			// Cross-parameter constraints must accept Object or Object[] as validated type
			final TypeMirror objectMirror = annotationApiHelper.getMirrorForType( Object.class );

			TypeMirror type = determineSupportedType( crossParameterValidator );
			Boolean supported = type.accept( new TypeKindVisitor8<Boolean, Void>() {
				@Override
				public Boolean visitArray(ArrayType t, Void p) {
					return typeUtils.isSameType( t.getComponentType(), objectMirror );
				}

				@Override
				public Boolean visitDeclared(DeclaredType t, Void p) {
					return typeUtils.isSameType( t, objectMirror );
				}
			}, null );

			if ( !supported ) {
				return ConstraintCheckResult.DISALLOWED;
			}
		}

		return ConstraintCheckResult.ALLOWED;
	}

	public Types getTypeUtils() {
		return typeUtils;
	}

	public boolean isSupportedForUnwrappingByDefault(Name typeName) {
		return supportedTypesUnwrappedByDefault.containsKey( typeName );
	}

	public Optional<TypeMirror> getUnwrappedToByDefault(Name typeName) {
		return Optional.ofNullable( supportedTypesUnwrappedByDefault.get( typeName ) );
	}

	// ==================================
	// private API below
	// ==================================

	/**
	 * Checks whether the given annotation mirror represents a constraint
	 * annotation or not. That's the case, if the given mirror is annotated with
	 * the {@code @Constraint} meta-annotation (which is only allowed at
	 * annotation declarations).
	 *
	 * @param annotationMirror The annotation mirror of interest.
	 *
	 * @return True, if the given mirror represents a constraint annotation
	 *         type, false otherwise.
	 */
	private boolean isConstraintAnnotation(AnnotationMirror annotationMirror) {
		return isConstraintAnnotation( annotationMirror.getAnnotationType().asElement() );
	}

	/**
	 * Checks whether the given annotation mirror represents the {@code @Constraint}
	 * meta-annotation or not.
	 *
	 * @param annotationMirror The annotation mirror of interest.
	 *
	 * @return True, if the given mirror represents the {@code @Constraint} meta-annotation
	 *         type, false otherwise.
	 */
	private boolean isConstraintMetaAnnotation(AnnotationMirror annotationMirror) {
		return getName( annotationMirror.getAnnotationType() ).contentEquals( BeanValidationTypes.CONSTRAINT );
	}

	/**
	 * Checks whether the given annotation mirror represents a multi-valued
	 * constraint such as {@link javax.validation.constraints.Pattern.List}.
	 * That is the case if the annotation has an array-typed attribute with name
	 * "value", that exclusively contains constraint annotations.
	 *
	 * @param annotationMirror The annotation mirror of interest.
	 *
	 * @return True, if the given mirror represents a multi-valued constraint,
	 *         false otherwise.
	 */
	private boolean isMultiValuedConstraint(AnnotationMirror annotationMirror) {

		List<? extends AnnotationValue> annotationArrayValue = annotationApiHelper.getAnnotationArrayValue(
				annotationMirror, "value"
		);

		// a multi-valued constraint must have at least one value
		if ( annotationArrayValue.isEmpty() ) {
			return false;
		}

		for ( AnnotationValue oneAnnotationValue : annotationArrayValue ) {

			Boolean isConstraintAnnotation = oneAnnotationValue.accept(
					new SimpleAnnotationValueVisitor8<Boolean, Void>() {

						@Override
						public Boolean visitAnnotation(
								AnnotationMirror a, Void p) {

							return isConstraintAnnotation( a.getAnnotationType().asElement() );
						}
					}, null
			);

			//TODO GM: have all parts of the array to be constraint annotations?
			if ( Boolean.TRUE != isConstraintAnnotation ) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks whether the given mirror represents the {@code Valid} annotation.
	 *
	 * @param annotationMirror The annotation mirror of interest.
	 *
	 * @return True, if the given mirror represents the {@code Valid} annotation, false
	 *         otherwise.
	 */
	private boolean isGraphValidationAnnotation(
			AnnotationMirror annotationMirror) {

		return getName( annotationMirror.getAnnotationType() ).contentEquals( BeanValidationTypes.VALID );
	}

	/**
	 * Checks whether the given mirror represents the {@code @GroupSequence} annotation.
	 *
	 * @param annotationMirror the annotation mirror of interest
	 * @return true, if the given mirror represents the {@code @GroupSequence} annotation, false otherwise
	 */
	private boolean isGroupSequenceAnnotation(
			AnnotationMirror annotationMirror) {

		return getName( annotationMirror.getAnnotationType() ).contentEquals( BeanValidationTypes.GROUP_SEQUENCE );
	}

	/**
	 * Checks whether the given mirror represents the {@code @GroupSequenceProvider} annotation.
	 *
	 * @param annotationMirror The annotation mirror of interest.
	 *
	 * @return True, if the given mirror represents the {@code @GroupSequenceProvider} annotation, false
	 *         otherwise.
	 */
	private boolean isGroupSequenceProviderAnnotation(
			AnnotationMirror annotationMirror) {

		return getName( annotationMirror.getAnnotationType() ).contentEquals( HibernateValidatorTypes.GROUP_SEQUENCE_PROVIDER );
	}

	private ConstraintCheckResult checkComposingConstraints(DeclaredType constraintAnnotationType, TypeMirror typeOfAnnotatedElement) {

		for ( AnnotationMirror oneComposingConstraint : getComposingConstraints( constraintAnnotationType ) ) {

			ConstraintCheckResult annotationCheckResult = checkConstraint(
					oneComposingConstraint.getAnnotationType(), typeOfAnnotatedElement
			);

			if ( annotationCheckResult != ConstraintCheckResult.ALLOWED ) {
				return annotationCheckResult;
			}

		}

		return ConstraintCheckResult.ALLOWED;
	}

	private ConstraintCheckResult checkSupportedTypes(DeclaredType constraintAnnotationType, TypeMirror typeOfAnnotatedElement) {

		Set<TypeMirror> supportedTypes = getSupportedTypes( constraintAnnotationType );
		Set<TypeMirror> assignableTypes = getAssignableTypes( supportedTypes, typeOfAnnotatedElement );

		//found more than one matching validator
		if ( assignableTypes.size() > 1 ) {
			return ConstraintCheckResult.MULTIPLE_VALIDATORS_FOUND;
		}
		//found exactly one matching validator OR the constraint is completely composed
		else if ( assignableTypes.size() == 1 ||
				( supportedTypes.size() == 0 && !getComposingConstraints( constraintAnnotationType ).isEmpty() ) ) {
			return ConstraintCheckResult.ALLOWED;
		}
		//found no matching validator
		else {
			return ConstraintCheckResult.DISALLOWED;
		}
	}

	/**
	 * <p>
	 * Returns a set with the types supported by the given constraint annotation
	 * type (as either retrieved from the validators given in the
	 * {@code @Constraint} meta-annotation or from the map with built-in
	 * constraints.
	 * </p>
	 * <p>
	 *
	 * @param constraintAnnotationType A constraint annotation type.
	 *
	 * @return A set with the supported types.
	 */
	private Set<TypeMirror> getSupportedTypes(DeclaredType constraintAnnotationType) {

		Name key = getName( constraintAnnotationType );
		Set<TypeMirror> supportedTypes = supportedTypesByConstraint.get( key );

		// create a mapping for the given annotation type if required
		if ( supportedTypes == null ) {
			supportedTypes = determineSupportedTypes( constraintAnnotationType );
			supportedTypesByConstraint.put( key, supportedTypes );
		}

		return supportedTypes;
	}

	private AnnotationProcessorValidationTarget resolveImplicitValidationTarget(ExecutableElement e) {

		if ( e.getParameters().isEmpty() ) {
			return AnnotationProcessorValidationTarget.ANNOTATED_ELEMENT;
		}
		else if ( e.getReturnType().getKind() == TypeKind.VOID ) {
			return AnnotationProcessorValidationTarget.PARAMETERS;
		}

		return null;
	}

	/**
	 * Determines the {@code AnnotationProcessorConstraintTarget} of the annotation.
	 *
	 * @param annotation the cross-parameter annotation
	 * @return the {@code AnnotationProcessorConstraintTarget}, if it defined using the 'validationAppliesTo()' attribute of the annotation, the default value if a valid 'validationAppliesTo()' attribute is defined, or null
	 */
	private AnnotationProcessorConstraintTarget getConstraintTarget(AnnotationMirror annotation) {

		AnnotationValue validationAppliesTo = annotationApiHelper.getAnnotationValue( annotation, "validationAppliesTo" );
		if ( validationAppliesTo == null ) {
			// validationAppliesTo not found on the annotation

			for ( Element e : annotation.getAnnotationType().asElement().getEnclosedElements() ) {

				Boolean isValidationAppliesToMethod = e.accept( new ElementKindVisitor8<Boolean, Void>() {
					@Override
					public Boolean visitExecutableAsMethod(ExecutableElement e, Void p) {
						if ( e.getSimpleName().contentEquals( "validationAppliesTo" ) ) {
							return true;
						}
						return false;
					}
				}, null );

				if ( Boolean.TRUE.equals( isValidationAppliesToMethod ) ) {
					// validationAppliesTo method is present, so the default value is returned (IMPLICIT)
					return AnnotationProcessorConstraintTarget.IMPLICIT;
				}
			}

			// cannot find the ConstraintTarget
			return null;
		}

		return validationAppliesTo.accept(
			new SimpleAnnotationValueVisitor8<AnnotationProcessorConstraintTarget, Void>() {

				private final TypeMirror constraintTargetMirror = annotationApiHelper.getDeclaredTypeByName( BeanValidationTypes.CONSTRAINT_TARGET );

				@Override
				public AnnotationProcessorConstraintTarget visitEnumConstant(VariableElement c, Void p) {
					if ( typeUtils.isSameType( c.asType(), constraintTargetMirror ) ) {
						return AnnotationProcessorConstraintTarget.valueOf( c.getSimpleName().toString() );
					}
					return null;
				}

			}, null
		);
	}


	private Set<TypeMirror> determineSupportedTypes(DeclaredType constraintAnnotationType) {

		return determineSupportedTypes( constraintAnnotationType, AnnotationProcessorValidationTarget.ANNOTATED_ELEMENT );
	}

	private Set<TypeMirror> determineSupportedTypes(DeclaredType constraintAnnotationType, AnnotationProcessorValidationTarget target) {

		//the Constraint meta-annotation at the type declaration, e.g. "@Constraint(validatedBy = CheckCaseValidator.class)"
		AnnotationMirror constraintMetaAnnotation = getConstraintMetaAnnotation( constraintAnnotationType );

		//the validator classes, e.g. [CheckCaseValidator.class]
		List<? extends AnnotationValue> validatorClassReferences = getValidatorClassesFromConstraintMetaAnnotation(
				constraintMetaAnnotation
		);

		Set<TypeMirror> supportedTypes = CollectionHelper.newHashSet();

		for ( AnnotationValue oneValidatorClassReference : validatorClassReferences ) {

			if ( isValidationTargetSupported( oneValidatorClassReference, target ) ) {
				TypeMirror supportedType = determineSupportedType( oneValidatorClassReference );
				supportedTypes.add( supportedType );
			}
		}

		return supportedTypes;
	}

	private TypeMirror determineSupportedType(AnnotationValue validatorClassReference) {

		// contains the bindings of the type parameters from the implemented
		// ConstraintValidator interface, e.g. "ConstraintValidator<CheckCase, String>"
		TypeMirror constraintValidatorImplementation = getConstraintValidatorSuperType( validatorClassReference );

		return constraintValidatorImplementation.accept(
				new TypeKindVisitor8<TypeMirror, Void>() {

					@Override
					public TypeMirror visitDeclared(DeclaredType constraintValidatorImplementation, Void p) {
						// 2nd type parameter contains the data type supported by current validator class, e.g. "String"
						return constraintValidatorImplementation.getTypeArguments().get( 1 );
					}

				}, null
		);
	}

	private boolean isValidationTargetSupported(AnnotationValue oneValidatorClassReference, AnnotationProcessorValidationTarget target) {
		return getSupportedValidationTargets( oneValidatorClassReference ).contains( target );
	}

	private Set<AnnotationProcessorValidationTarget> getSupportedValidationTargets(AnnotationValue oneValidatorClassReference) {
		// determine the class that could contain the @SupportedValidationTarget annotation.
		TypeMirror validatorClass = oneValidatorClassReference.accept(
				new SimpleAnnotationValueVisitor8<TypeMirror, Void>() {

					@Override
					public TypeMirror visitType(TypeMirror t, Void p) {
						return t;
					}
				}, null
		);

		DeclaredType validatorType = validatorClass.accept( new TypeKindVisitor8<DeclaredType, Void>() {
			@Override
			public DeclaredType visitDeclared(DeclaredType t, Void p) {
				return t;
			}
		}, null );

		DeclaredType supportedValidationTargetType = annotationApiHelper.getDeclaredTypeByName( BeanValidationTypes.SUPPORTED_VALIDATION_TARGET );

		AnnotationMirror supportedTargetDecl = null;

		for ( AnnotationMirror mirr : validatorType.asElement().getAnnotationMirrors() ) {
			if ( typeUtils.isSameType( mirr.getAnnotationType(), supportedValidationTargetType ) ) {
				supportedTargetDecl = mirr;
				break;
			}
		}

		EnumSet<AnnotationProcessorValidationTarget> allowedTargets = EnumSet.noneOf( AnnotationProcessorValidationTarget.class );

		if ( supportedTargetDecl == null ) {
			// If @SupportedValidationTarget is not present, the ConstraintValidator targets the (returned) element annotated by the constraint.
			allowedTargets.add( AnnotationProcessorValidationTarget.ANNOTATED_ELEMENT );
		}
		else {
			List<? extends AnnotationValue> values = annotationApiHelper.getAnnotationArrayValue( supportedTargetDecl, "value" );
			for ( AnnotationValue val : values ) {
				AnnotationProcessorValidationTarget target = val.accept( new SimpleAnnotationValueVisitor8<AnnotationProcessorValidationTarget, Void>() {
					@Override
					public AnnotationProcessorValidationTarget visitEnumConstant(VariableElement c, Void p) {
						return AnnotationProcessorValidationTarget.valueOf( c.getSimpleName().toString() );
					}
				}, null );

				allowedTargets.add( target );
			}

		}

		return allowedTargets;
	}


	private TypeMirror getConstraintValidatorSuperType(AnnotationValue oneValidatorClassReference) {

		TypeMirror type = oneValidatorClassReference.accept(
				new SimpleAnnotationValueVisitor8<TypeMirror, Void>() {

					@Override
					public TypeMirror visitType(TypeMirror t, Void p) {
						return t;
					}
				}, null
		);

		List<? extends TypeMirror> superTypes = typeUtils.directSupertypes( type );
		List<TypeMirror> nextSuperTypes = CollectionHelper.newArrayList();

		//follow the type hierarchy upwards, until we have found the ConstraintValidator IF
		while ( !superTypes.isEmpty() ) {

			for ( TypeMirror oneSuperType : superTypes ) {
				if ( getName( typeUtils.asElement( oneSuperType ) ).contentEquals( BeanValidationTypes.CONSTRAINT_VALIDATOR ) ) {
					return oneSuperType;
				}

				nextSuperTypes.addAll( typeUtils.directSupertypes( oneSuperType ) );
			}

			superTypes = nextSuperTypes;
			nextSuperTypes = CollectionHelper.newArrayList();
		}

		//HV-293: Actually this should never happen, as we can have only ConstraintValidator implementations
		//here. The Eclipse JSR 269 implementation unfortunately doesn't always create the type hierarchy
		//properly though.
		//TODO GM: create and report an isolated test case
		throw new IllegalStateException( "Expected type " + type + " to implement javax.validation.ConstraintValidator, but it doesn't." );
	}

	/**
	 * Retrieves the {@code @Constraint} meta-annotation from the given
	 * constraint annotation.
	 *
	 * @param annotationType A constraint type.
	 *
	 * @return The Constraint meta-annotation.
	 *
	 * @throws IllegalArgumentException If the given constraint annotation type isn't annotated with
	 * the {@code @Constraint} meta-annotation.
	 */
	private AnnotationMirror getConstraintMetaAnnotation(DeclaredType annotationType) {

		List<? extends AnnotationMirror> annotationMirrors = annotationType.asElement().getAnnotationMirrors();

		AnnotationMirror constraintMetaAnnotation = annotationApiHelper.getMirror(
				annotationMirrors, BeanValidationTypes.CONSTRAINT
		);

		if ( constraintMetaAnnotation == null ) {
			throw new IllegalArgumentException( "Given type " + annotationType + " isn't a constraint annotation type." );
		}

		return constraintMetaAnnotation;
	}

	private List<? extends AnnotationValue> getValidatorClassesFromConstraintMetaAnnotation(AnnotationMirror constraintMetaAnnotation) {

		AnnotationValue validatedBy = annotationApiHelper.getAnnotationValue( constraintMetaAnnotation, "validatedBy" );

		return validatedBy.accept(
				new SimpleAnnotationValueVisitor8<List<? extends AnnotationValue>, Void>() {

					@Override
					public List<? extends AnnotationValue> visitArray(List<? extends AnnotationValue> values, Void p) {
						return values;
					}

				}, null
		);
	}

	/**
	 * Returns a set containing those types from the given set of types to which
	 * the given type is assignable. If the given type can be assigned to
	 * multiple types from the same inheritance hierarchy (e.g. Collection and
	 * Set), only the "lowest" one (e.g. Set) will be part of the result.
	 *
	 * @param types The types to check.
	 * @param type The type to check.
	 *
	 * @return A set containing those types from the given set of types to which
	 *         the given type is assignable.
	 */
	private Set<TypeMirror> getAssignableTypes(Set<TypeMirror> types, TypeMirror type) {

		Set<TypeMirror> theValue = CollectionHelper.newHashSet();

		for ( TypeMirror supportedType : types ) {
			if ( typeUtils.isAssignable( type, supportedType ) ) {
				theValue.add( supportedType );
			}
		}

		return annotationApiHelper.keepLowestTypePerHierarchy( theValue );
	}

	private void registerAllowedTypesForBuiltInConstraint(String annotationType, Class<?>... types) {
		registerAllowedTypesForBuiltInConstraint( annotationType, asMirrors( types ) );
	}

	private void registerAllowedTypesForBuiltInConstraint(String annotationType, String... typeNames) {
		registerAllowedTypesForBuiltInConstraint( annotationType, asMirrors( typeNames ) );
	}

	private void registerAllowedTypesForBuiltInConstraint(String annotationType, List<TypeMirror> supportedTypes) {

		DeclaredType annotation = annotationApiHelper.getDeclaredTypeByName( annotationType );

		if ( annotation == null ) {
			return;
		}
		Name key = getName( annotation );
		Set<TypeMirror> types = supportedTypesByConstraint.get( key );

		if ( types == null ) {
			supportedTypesByConstraint.put( key, new HashSet<>( supportedTypes ) );
		}
		else {
			types.addAll( supportedTypes );
		}
	}

	private void registerSupportedTypesUnwrappedByDefault(String typeName, String unwrappedToTypeName) {
		DeclaredType typeToUnwrap = annotationApiHelper.getDeclaredTypeByName( typeName );

		if ( typeToUnwrap == null ) {
			return;
		}

		supportedTypesUnwrappedByDefault.put(
				getName( typeToUnwrap ),
				annotationApiHelper.getDeclaredTypeByName( unwrappedToTypeName )
		);
	}

	private Name getName(DeclaredType type) {
		return getName( type.asElement() );
	}

	private Name getName(Element element) {
		if ( element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.INTERFACE || element.getKind() == ElementKind.ANNOTATION_TYPE ) {
			return ( (TypeElement) element ).getQualifiedName();
		}
		else {
			return element.getSimpleName();
		}
	}

	private Set<AnnotationMirror> getComposingConstraints(DeclaredType constraintAnnotationType) {

		Name key = getName( constraintAnnotationType );

		Set<AnnotationMirror> composingConstraints = composingConstraintsByConstraints.get( key );

		if ( composingConstraints != null ) {
			return composingConstraints;
		}

		composingConstraints = CollectionHelper.newHashSet();

		List<? extends AnnotationMirror> annotationMirrors = constraintAnnotationType.asElement()
				.getAnnotationMirrors();

		for ( AnnotationMirror oneAnnotationMirror : annotationMirrors ) {

			AnnotationType annotationType = getAnnotationType( oneAnnotationMirror );

			if ( annotationType == AnnotationType.CONSTRAINT_ANNOTATION ) {
				composingConstraints.add( oneAnnotationMirror );
			}
			else if ( annotationType == AnnotationType.MULTI_VALUED_CONSTRAINT_ANNOTATION ) {
				List<? extends AnnotationValue> value = annotationApiHelper.getAnnotationArrayValue(
						oneAnnotationMirror,
						"value"
				);
				for ( AnnotationValue annotationValue : value ) {
					composingConstraints.add( (AnnotationMirror) annotationValue );
				}
			}
		}

		composingConstraintsByConstraints.put( key, composingConstraints );

		return composingConstraints;
	}

	private List<TypeMirror> asMirrors(Class<?>... types) {

		List<TypeMirror> mirrors = new ArrayList<>( types.length );

		for ( Class<?> oneType : types ) {

			TypeMirror oneMirror = annotationApiHelper.getMirrorForType( oneType );

			if ( oneMirror != null ) {
				mirrors.add( oneMirror );
			}
		}

		return mirrors;
	}

	private List<TypeMirror> asMirrors(String... typeNames) {

		List<TypeMirror> mirrors = new ArrayList<>( typeNames.length );

		for ( String oneTypeName : typeNames ) {

			TypeMirror oneMirror = annotationApiHelper.getDeclaredTypeByName( oneTypeName );

			if ( oneMirror != null ) {
				mirrors.add( oneMirror );
			}
		}

		return mirrors;
	}
}
