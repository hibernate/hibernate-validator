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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.TypeKindVisitor6;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.util.TypeNames.BeanValidationTypes;
import org.hibernate.validator.ap.util.TypeNames.HibernateValidatorTypes;
import org.hibernate.validator.ap.util.TypeNames.JodaTypes;

/**
 * Helper class that deals with all constraint-related stuff, such as
 * determining whether a given annotation represents a constraint annotation or
 * whether a given annotation is allowed to be declared at a given element.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
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
	 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
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
		 * Given annotation is the {@code @Valid} annotation.
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
	 * Contains the supported types for given constraints. Keyed by constraint
	 * annotation type names, each value is a set with the allowed types for the
	 * mapped constraint. The map is pre-populated with the supported
	 * constraints for the built-in constraints. Constraints for custom
	 * constraints are determined and stored on demand.
	 */
	private final Map<Name, Set<TypeMirror>> supportedTypesByConstraint;

	private final Map<Name, AnnotationType> annotationTypeCache;

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

		//register BV-defined constraints
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.ASSERT_FALSE, Boolean.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.ASSERT_TRUE, Boolean.class );
		registerAllowedTypesForBuiltInConstraint(
				BeanValidationTypes.DECIMAL_MAX,
				Number.class,
				String.class
		);
		registerAllowedTypesForBuiltInConstraint(
				BeanValidationTypes.DECIMAL_MIN,
				Number.class,
				String.class
		);
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.DIGITS, Number.class, String.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.FUTURE, Calendar.class, Date.class );
		registerAllowedTypesForBuiltInConstraint(
				BeanValidationTypes.FUTURE,
				JodaTypes.READABLE_PARTIAL,
				JodaTypes.READABLE_INSTANT
		);
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.MAX, Number.class, String.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.MIN, Number.class, String.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.NOT_NULL, Object.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.NULL, Object.class );
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.PAST, Calendar.class, Date.class );
		registerAllowedTypesForBuiltInConstraint(
				BeanValidationTypes.PAST,
				JodaTypes.READABLE_PARTIAL,
				JodaTypes.READABLE_INSTANT
		);
		registerAllowedTypesForBuiltInConstraint( BeanValidationTypes.PATTERN, String.class );
		registerAllowedTypesForBuiltInConstraint(
				BeanValidationTypes.SIZE,
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
		);

		//register HV-specific constraints
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.EMAIL, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.LENGTH, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.MOD_CHECK, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.LUHN_CHECK, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.MOD_10_CHECK, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.MOD_11_CHECK, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.NOT_BLANK, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.SAFE_HTML, CharSequence.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.SCRIPT_ASSERT, Object.class );
		registerAllowedTypesForBuiltInConstraint( HibernateValidatorTypes.URL, CharSequence.class );
	}

	/**
	 * Checks, whether the given type element represents a constraint annotation
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
	 * Checks, whether the given annotation mirror represents the {@code @Constraint}
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
	 * Checks, whether the given mirror represents the {@code Valid} annotation.
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
	 * Checks, whether the given mirror represents the {@code @GroupSequenceProvider} annotation.
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

	private Set<TypeMirror> determineSupportedTypes(DeclaredType constraintAnnotationType) {

		//the Constraint meta-annotation at the type declaration, e.g. "@Constraint(validatedBy = CheckCaseValidator.class)"
		AnnotationMirror constraintMetaAnnotation = getConstraintMetaAnnotation( constraintAnnotationType );

		//the validator classes, e.g. [CheckCaseValidator.class]
		List<? extends AnnotationValue> validatorClassReferences = getValidatorClassesFromConstraintMetaAnnotation(
				constraintMetaAnnotation
		);

		Set<TypeMirror> supportedTypes = CollectionHelper.newHashSet();

		for ( AnnotationValue oneValidatorClassReference : validatorClassReferences ) {

			TypeMirror supportedType = getSupportedType( oneValidatorClassReference );
			supportedTypes.add( supportedType );
		}

		return supportedTypes;
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
				new SimpleAnnotationValueVisitor6<List<? extends AnnotationValue>, Void>() {

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
			supportedTypesByConstraint.put( key, new HashSet<TypeMirror>( supportedTypes ) );
		}
		else {
			types.addAll( supportedTypes );
		}
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

		if( composingConstraints != null ) {
			return composingConstraints;
		}

		composingConstraints = CollectionHelper.newHashSet();

		List<? extends AnnotationMirror> annotationMirrors = constraintAnnotationType.asElement()
				.getAnnotationMirrors();

		for ( AnnotationMirror oneAnnotationMirror : annotationMirrors ) {

			AnnotationType annotationType = getAnnotationType(oneAnnotationMirror);

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

		List<TypeMirror> mirrors = new ArrayList<TypeMirror>( types.length );

		for ( Class<?> oneType : types ) {

			TypeMirror oneMirror = annotationApiHelper.getMirrorForType( oneType );

			if ( oneMirror != null ) {
				mirrors.add( oneMirror );
			}
		}

		return mirrors;
	}

	private List<TypeMirror> asMirrors(String... typeNames) {

		List<TypeMirror> mirrors = new ArrayList<TypeMirror>( typeNames.length );

		for ( String oneTypeName : typeNames ) {

			TypeMirror oneMirror = annotationApiHelper.getDeclaredTypeByName( oneTypeName );

			if ( oneMirror != null ) {
				mirrors.add( oneMirror );
			}
		}

		return mirrors;
	}
}
