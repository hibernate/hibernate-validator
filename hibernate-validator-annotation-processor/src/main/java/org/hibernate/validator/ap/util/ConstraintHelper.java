/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.ap.util;

import java.lang.annotation.Annotation;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.TypeKindVisitor6;
import javax.lang.model.util.Types;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.Valid;
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

import org.hibernate.validator.group.GroupSequenceProvider;

/**
 * Helper class that deals with all constraint-related stuff, such as
 * determining whether a given annotation represents a constraint annotation or
 * whether a given annotation is allowed to be declared at a given element.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
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
	 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
	 */
	public enum AnnotationType {

		/**
		 * Given annotation is a constraint annotation (e.g. @Min).
		 */
		CONSTRAINT_ANNOTATION,

		/**
		 * Given annotation is a multi-valued annotation (e.g.
		 * <code>
		 * &#64;List({
		 * &#64;Min(10),
		 * &#64;Min(value=20, groups= Special.class})
		 * })
		 * </code>.
		 */
		MULTI_VALUED_CONSTRAINT_ANNOTATION,

		/**
		 * Given annotation is the @Valid annotation.
		 */
		GRAPH_VALIDATION_ANNOTATION,

		/**
		 * Given annotation is the @Constraint meta-annotation.
		 */
		CONSTRAINT_META_ANNOTATION,

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
	 * The name of the package containing JSR 303 standard annotations
	 * ("javax.validation.constraints").
	 */
	private final Name CONSTRAINT_ANNOTATION_PACKAGE_NAME;

	/**
	 * Contains the supported types for the built-in constraints. Keyed by
	 * constraint annotation type names, each value is a set with the allowed
	 * types for the mapped constraint.
	 */
	private Map<Name, Set<TypeMirror>> builtInConstraints;

	private Elements elementUtils;

	private Types typeUtils;

	private AnnotationApiHelper annotationApiHelper;

	public ConstraintHelper(Elements elementUtils, Types typeUtils, AnnotationApiHelper annotationApiHelper) {

		this.elementUtils = elementUtils;
		this.typeUtils = typeUtils;
		this.annotationApiHelper = annotationApiHelper;

		CONSTRAINT_ANNOTATION_PACKAGE_NAME = elementUtils.getName( Size.class.getPackage().getName() );

		builtInConstraints = CollectionHelper.newHashMap();

		registerAllowedTypesForBuiltInConstraint(
				AssertFalse.class, CollectionHelper.<Class<?>>asSet( Boolean.class )
		);
		registerAllowedTypesForBuiltInConstraint( AssertTrue.class, CollectionHelper.<Class<?>>asSet( Boolean.class ) );
		registerAllowedTypesForBuiltInConstraint(
				DecimalMax.class, CollectionHelper.<Class<?>>asSet( Number.class, String.class )
		);
		registerAllowedTypesForBuiltInConstraint(
				DecimalMin.class, CollectionHelper.<Class<?>>asSet( Number.class, String.class )
		);
		registerAllowedTypesForBuiltInConstraint(
				Digits.class, CollectionHelper.<Class<?>>asSet( Number.class, String.class )
		);
		registerAllowedTypesForBuiltInConstraint(
				Future.class, CollectionHelper.<Class<?>>asSet( Calendar.class, Date.class )
		);
		registerAllowedTypesForBuiltInConstraintByNames(
				Future.class,
				CollectionHelper.<String>asSet( "org.joda.time.ReadablePartial", "org.joda.time.ReadableInstant" )
		);
		registerAllowedTypesForBuiltInConstraint(
				Max.class, CollectionHelper.<Class<?>>asSet( Number.class, String.class )
		);
		registerAllowedTypesForBuiltInConstraint(
				Min.class, CollectionHelper.<Class<?>>asSet( Number.class, String.class )
		);
		registerAllowedTypesForBuiltInConstraint( NotNull.class, CollectionHelper.<Class<?>>asSet( Object.class ) );
		registerAllowedTypesForBuiltInConstraint( Null.class, CollectionHelper.<Class<?>>asSet( Object.class ) );
		registerAllowedTypesForBuiltInConstraint(
				Past.class, CollectionHelper.<Class<?>>asSet( Calendar.class, Date.class )
		);
		registerAllowedTypesForBuiltInConstraintByNames(
				Past.class, 
				CollectionHelper.<String>asSet( "org.joda.time.ReadablePartial", "org.joda.time.ReadableInstant" )
		);

		registerAllowedTypesForBuiltInConstraint( Pattern.class, CollectionHelper.<Class<?>>asSet( String.class ) );
		registerAllowedTypesForBuiltInConstraint(
				Size.class, CollectionHelper.<Class<?>>asSet(
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
				String.class
		)
		);
	}

	/**
	 * Checks, whether the given type element represents a constraint annotation
	 * or not. That's the case, if the given element is annotated with the
	 * {@link Constraint} meta-annotation (which is only allowed at annotation
	 * declarations).
	 *
	 * @param element The element of interest.
	 *
	 * @return True, if the given element is a constraint annotation type, false
	 *         otherwise.
	 */
	public boolean isConstraintAnnotation(Element element) {
		return element.getAnnotation( Constraint.class ) != null;
	}

	/**
	 * Returns the {@link AnnotationType} of the given annotation.
	 *
	 * @param annotationMirror The annotation mirror of interest.
	 *
	 * @return The given mirror's annotation type.
	 */
	public AnnotationType getAnnotationType(AnnotationMirror annotationMirror) {

		if ( isConstraintAnnotation( annotationMirror ) ) {
			return AnnotationType.CONSTRAINT_ANNOTATION;
		}
		else if ( isMultiValuedConstraint( annotationMirror ) ) {
			return AnnotationType.MULTI_VALUED_CONSTRAINT_ANNOTATION;
		}
		else if ( isGraphValidationAnnotation( annotationMirror ) ) {
			return AnnotationType.GRAPH_VALIDATION_ANNOTATION;
		}
		else if ( isConstraintMetaAnnotation( annotationMirror ) ) {
			return AnnotationType.CONSTRAINT_META_ANNOTATION;
		}
		else if ( isGroupSequenceProviderAnnotation( annotationMirror ) ) {
			return AnnotationType.GROUP_SEQUENCE_PROVIDER_ANNOTATION;
		}
		else {
			return AnnotationType.NO_CONSTRAINT_ANNOTATION;
		}

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
					new SimpleAnnotationValueVisitor6<Void, Void>() {

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
	public ConstraintCheckResult checkConstraint(
			DeclaredType constraintAnnotationType,
			TypeMirror typeOfAnnotatedElement) {

		return isBuiltInConstraint( constraintAnnotationType ) ? checkBuiltInConstraint(
				constraintAnnotationType, typeOfAnnotatedElement
		)
				: checkCustomConstraint(
				constraintAnnotationType,
				typeOfAnnotatedElement
		);
	}

	/**
	 * Checks, whether the given type element represents a composed constraint or not.
	 *
	 * @param element The type element of interest. Must not be null.
	 *
	 * @return True if the given element represents a composed constraint, false otherwise.
	 */
	public boolean isComposedConstraint(TypeElement element) {

		return Boolean.TRUE.equals(
				element.asType().accept(
						new TypeKindVisitor6<Boolean, Void>() {

							@Override
							public Boolean visitDeclared(DeclaredType constraintValidatorImplementation, Void p) {
								return !getComposingConstraints( constraintValidatorImplementation ).isEmpty();
							}

						}, null
				)
		);
	}

	// ==================================
	// private API below
	// ==================================

	/**
	 * Checks, whether the given annotation mirror represents a constraint
	 * annotation or not. That's the case, if the given mirror is annotated with
	 * the {@link Constraint} meta-annotation (which is only allowed at
	 * annotation declarations).
	 *
	 * @param annotationMirror The annotation mirror of interest.
	 *
	 * @return True, if the given mirror represents a constraint annotation
	 *         type, false otherwise.
	 */
	private boolean isConstraintAnnotation(AnnotationMirror annotationMirror) {
		return isConstraintAnnotation(
				annotationMirror.getAnnotationType()
						.asElement()
		);
	}

	/**
	 * Checks, whether the given annotation mirror represents the {@link javax.validation.Constraint}
	 * meta-annotation or not.
	 *
	 * @param annotationMirror The annotation mirror of interest.
	 *
	 * @return True, if the given mirror represents the @Constraint meta-annotation
	 *         type, false otherwise.
	 */
	private boolean isConstraintMetaAnnotation(AnnotationMirror annotationMirror) {
		return annotationMirror.getAnnotationType().asElement().getSimpleName().contentEquals( "Constraint" );
	}

	/**
	 * Checks, whether the given annotation mirror represents a multi-valued
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
					new SimpleAnnotationValueVisitor6<Boolean, Void>() {

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
	 * Checks, whether the given mirror represents the {@link Valid} annotation.
	 *
	 * @param annotationMirror The annotation mirror of interest.
	 *
	 * @return True, if the given mirror represents the @Valid annotation, false
	 *         otherwise.
	 */
	private boolean isGraphValidationAnnotation(
			AnnotationMirror annotationMirror) {

		return typeUtils.isSameType(
				annotationMirror.getAnnotationType(),
				annotationApiHelper.getMirrorForType( Valid.class )
		);
	}

	/**
	 * Checks, whether the given mirror represents the {@link GroupSequenceProvider} annotation.
	 *
	 * @param annotationMirror The annotation mirror of interest.
	 *
	 * @return True, if the given mirror represents the @GroupSequenceProvider annotation, false
	 *         otherwise.
	 */
	private boolean isGroupSequenceProviderAnnotation(
			AnnotationMirror annotationMirror) {

		return typeUtils.isSameType(
				annotationMirror.getAnnotationType(),
				annotationApiHelper.getMirrorForType( GroupSequenceProvider.class )
		);
	}

	private ConstraintCheckResult checkBuiltInConstraint(DeclaredType builtInAnnotationType, TypeMirror typeOfAnnotatedElement) {

		Set<TypeMirror> allowedTypes = getAllowedTypesForBuiltInConstraint( builtInAnnotationType );

		for ( TypeMirror oneAllowedType : allowedTypes ) {
			if ( typeUtils.isAssignable( typeOfAnnotatedElement, oneAllowedType ) ) {
				return ConstraintCheckResult.ALLOWED;
			}
		}

		return ConstraintCheckResult.DISALLOWED;
	}

	private Set<TypeMirror> getAllowedTypesForBuiltInConstraint(DeclaredType builtInAnnotationType) {

		Set<TypeMirror> theValue = builtInConstraints.get( builtInAnnotationType.asElement().getSimpleName() );

		if ( theValue == null ) {
			theValue = Collections.emptySet();
		}

		return theValue;
	}

	/**
	 * Returns a set containing all those types, at which the specified custom
	 * constraint-annotation is allowed.
	 *
	 * @param customAnnotationType A custom constraint type.
	 * @param typeOfAnnotatedElement The type of the annotated element
	 *
	 * @return A set with all types supported by the given constraint. May be
	 *         empty in case of constraint composition, if there is no common
	 *         type supported by all composing constraints.
	 */
	private ConstraintCheckResult checkCustomConstraint(DeclaredType customAnnotationType, TypeMirror typeOfAnnotatedElement) {

		Set<AnnotationMirror> composingConstraints = getComposingConstraints( customAnnotationType );
		boolean isComposedConstraint = !composingConstraints.isEmpty();

		for ( AnnotationMirror oneComposingConstraint : composingConstraints ) {

			ConstraintCheckResult annotationCheckResult = checkConstraint(
					oneComposingConstraint.getAnnotationType(), typeOfAnnotatedElement
			);

			if ( annotationCheckResult != ConstraintCheckResult.ALLOWED ) {
				return annotationCheckResult;
			}

		}

		Set<TypeMirror> theValue = getSupportedTypes(
				customAnnotationType,
				typeOfAnnotatedElement
		);

		if ( theValue.size() > 1 ) {
			return ConstraintCheckResult.MULTIPLE_VALIDATORS_FOUND;
		}
		else if ( theValue.size() == 1 || isComposedConstraint ) {
			return ConstraintCheckResult.ALLOWED;
		}
		else {
			return ConstraintCheckResult.DISALLOWED;
		}
	}

	/**
	 * <p> Returns a set with those types supported by the constraint validators
	 * specified in the @Constraint meta-annotation of the given constraint
	 * annotation type to which the specified type can be assigned. </p>
	 * <p>
	 * If multiple types from the same inheritance hierarchy (e.g. Collection
	 * and Set) are supported by the validators, only the "lowest" one (e.g.
	 * Set) will be part of the result.
	 * </p>
	 *
	 * @param constraintAnnotationType A constraint annotation type.
	 * @param type A type.
	 *
	 * @return A set with the supported types.
	 */
	private Set<TypeMirror> getSupportedTypes(
			DeclaredType constraintAnnotationType, TypeMirror type) {

		Set<TypeMirror> theValue = CollectionHelper.newHashSet();

		//the Constraint meta-annotation at the type declaration, e.g. "@Constraint(validatedBy = CheckCaseValidator.class)"
		AnnotationMirror constraintMetaAnnotation = getConstraintMetaAnnotation( constraintAnnotationType );

		//the validator classes, e.g. [CheckCaseValidator.class]
		List<? extends AnnotationValue> validatorClassReferences = getValidatorClassesFromConstraintMetaAnnotation(
				constraintMetaAnnotation
		);

		for ( AnnotationValue oneValidatorClassReference : validatorClassReferences ) {

			TypeMirror supportedType = getSupportedType( oneValidatorClassReference );

			if ( typeUtils.isAssignable( type, supportedType ) ) {
				theValue.add( supportedType );
			}
		}

		return annotationApiHelper.keepLowestTypePerHierarchy( theValue );
	}

	private TypeMirror getSupportedType(AnnotationValue oneValidatorClassReference) {

		TypeMirror validatorType = oneValidatorClassReference.accept(
				new SimpleAnnotationValueVisitor6<TypeMirror, Void>() {

					@Override
					public TypeMirror visitType(TypeMirror t, Void p) {
						return t;
					}
				}, null
		);

		// contains the bindings of the type parameters from the implemented
		// ConstraintValidator interface, e.g. "ConstraintValidator<CheckCase, String>"
		TypeMirror constraintValidatorImplementation = getConstraintValidatorSuperType( validatorType );

		return constraintValidatorImplementation.accept(
				new TypeKindVisitor6<TypeMirror, Void>() {

					@Override
					public TypeMirror visitDeclared(DeclaredType constraintValidatorImplementation, Void p) {
						// 2nd type parameter contains the data type supported by current validator class, e.g. "String"
						return constraintValidatorImplementation.getTypeArguments().get( 1 );
					}

				}, null
		);
	}

	private TypeMirror getConstraintValidatorSuperType(TypeMirror type) {

		List<? extends TypeMirror> superTypes = typeUtils.directSupertypes( type );
		List<TypeMirror> nextSuperTypes = CollectionHelper.newArrayList();

		//follow the type hierarchy upwards, until we have found the ConstraintValidator IF
		while ( !superTypes.isEmpty() ) {

			for ( TypeMirror oneSuperType : superTypes ) {
				if ( typeUtils.asElement( oneSuperType ).getSimpleName()
						.contentEquals( ConstraintValidator.class.getSimpleName() ) ) {

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
	 * Retrieves the {@link Constraint} meta-annotation from the given
	 * constraint annotation.
	 *
	 * @param annotationType A constraint type.
	 *
	 * @return The Constraint meta-annotation.
	 *
	 * @throws IllegalArgumentException If the given constraint annotation type isn't annotated with
	 *                                  the {@link Constraint} meta-annotation.
	 */
	private AnnotationMirror getConstraintMetaAnnotation(DeclaredType annotationType) {

		List<? extends AnnotationMirror> annotationMirrors = annotationType.asElement().getAnnotationMirrors();

		AnnotationMirror constraintMetaAnnotation = annotationApiHelper.getMirror(
				annotationMirrors, Constraint.class
		);

		if ( constraintMetaAnnotation == null ) {
			throw new IllegalArgumentException( "Given type " + annotationType + " isn't a constraint annotation type." );
		}

		return constraintMetaAnnotation;
	}

	private List<? extends AnnotationValue> getValidatorClassesFromConstraintMetaAnnotation(AnnotationMirror constraintMetaAnnotation) {

		AnnotationValue validatedBy = annotationApiHelper.getAnnotationValue( constraintMetaAnnotation, "validatedBy" );

		return validatedBy.accept(
				new SimpleAnnotationValueVisitor6<List<? extends AnnotationValue>, Void>() {

					@Override
					public List<? extends AnnotationValue> visitArray(List<? extends AnnotationValue> values, Void p) {
						return values;
					}

				}, null
		);
	}

	private void registerAllowedTypesForBuiltInConstraint(Class<? extends Annotation> annotation, Set<Class<?>> allowedTypes) {

		Set<TypeMirror> allowedTypesForConstraint = getAllowedTypesForBuiltInConstraint( annotation );

		for ( Class<?> oneAllowedType : allowedTypes ) {
			allowedTypesForConstraint.add( annotationApiHelper.getMirrorForType( oneAllowedType ) );
		}
	}

	private void registerAllowedTypesForBuiltInConstraintByNames(Class<? extends Annotation> annotation, Set<String> allowedTypes) {

		Set<TypeMirror> allowedTypesForConstraint = getAllowedTypesForBuiltInConstraint( annotation );

		for ( String oneAllowedType : allowedTypes ) {
			allowedTypesForConstraint.add( annotationApiHelper.getDeclaredTypeByName( oneAllowedType ) );
		}
	}

	private Set<TypeMirror> getAllowedTypesForBuiltInConstraint(Class<? extends Annotation> annotation) {

		Name key = elementUtils.getName( annotation.getSimpleName() );
		Set<TypeMirror> allowedTypes = builtInConstraints.get( key );

		// create a mapping for the given annotation type if required
		if ( allowedTypes == null ) {
			allowedTypes = CollectionHelper.newHashSet();
			builtInConstraints.put( key, allowedTypes );
		}

		return allowedTypes;
	}

	/**
	 * Checks, whether the given constraint annotation type is a built-in
	 * constraint annotation (which is the case, if it is declared in the
	 * package <code>javax.validation.constraints</code>).
	 *
	 * @param constraintAnnotationType The type to check.
	 *
	 * @return True, if the given type is a built-in constraint annotation type,
	 *         false otherwise.
	 */
	private boolean isBuiltInConstraint(DeclaredType constraintAnnotationType) {

		return
				CONSTRAINT_ANNOTATION_PACKAGE_NAME.equals(
						elementUtils.getPackageOf( constraintAnnotationType.asElement() ).getQualifiedName()
				);
	}

	private Set<AnnotationMirror> getComposingConstraints(DeclaredType constraintAnnotationType) {

		Set<AnnotationMirror> theValue = CollectionHelper.newHashSet();

		List<? extends AnnotationMirror> annotationMirrors = constraintAnnotationType.asElement()
				.getAnnotationMirrors();

		for ( AnnotationMirror oneAnnotationMirror : annotationMirrors ) {
			if ( isConstraintAnnotation( oneAnnotationMirror.getAnnotationType().asElement() ) ) {
				theValue.add( oneAnnotationMirror );
			}
		}

		return theValue;
	}

}
