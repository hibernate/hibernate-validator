// $Id: ConstraintHelper.java 17946 2009-11-06 18:23:48Z hardy.ferentschik $
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
import java.util.Map.Entry;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.TypeKindVisitor6;
import javax.lang.model.util.Types;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
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

/**
 * Helper class that deals with all constraint-related stuff, such as
 * determining whether a given annotation represents a constraint annotation or
 * whether a given annotation is allowed to be declared at a given element.
 *
 * @author Gunnar Morling
 *
 */
public class ConstraintHelper {

	/**
	 * The name of the package containing JSR 303 standard annotations
	 * ("javax.validation.constraints").
	 */
	private final Name CONSTRAINT_ANNOTATION_PACKAGE_NAME;

	private static Map<Name, Set<TypeMirror>> builtInConstraints;

	private Elements elementUtils;

	private Types typeUtils;

	public ConstraintHelper(Elements elementUtils, Types typeUtils) {

		this.elementUtils = elementUtils;
		this.typeUtils = typeUtils;

		CONSTRAINT_ANNOTATION_PACKAGE_NAME = elementUtils.getName(Size.class.getPackage().getName());

		builtInConstraints = CollectionHelper.newHashMap();

		registerAllowedTypesForBuiltInConstraint(AssertFalse.class, CollectionHelper.<Class<?>>asSet(Boolean.class));
		registerAllowedTypesForBuiltInConstraint(AssertTrue.class, CollectionHelper.<Class<?>>asSet(Boolean.class));
		registerAllowedTypesForBuiltInConstraint(DecimalMax.class, CollectionHelper.<Class<?>>asSet(Number.class, String.class));
		registerAllowedTypesForBuiltInConstraint(DecimalMin.class, CollectionHelper.<Class<?>>asSet(Number.class, String.class));
		registerAllowedTypesForBuiltInConstraint(Digits.class, CollectionHelper.<Class<?>>asSet(Number.class, String.class));
		registerAllowedTypesForBuiltInConstraint(Future.class, CollectionHelper.<Class<?>>asSet(Calendar.class, Date.class));
		registerAllowedTypesForBuiltInConstraint(Max.class, CollectionHelper.<Class<?>>asSet(Number.class, String.class));
		registerAllowedTypesForBuiltInConstraint(Min.class, CollectionHelper.<Class<?>>asSet(Number.class, String.class));
		registerAllowedTypesForBuiltInConstraint(NotNull.class, CollectionHelper.<Class<?>>asSet(Object.class));
		registerAllowedTypesForBuiltInConstraint(Null.class, CollectionHelper.<Class<?>>asSet(Object.class));
		registerAllowedTypesForBuiltInConstraint(Past.class, CollectionHelper.<Class<?>>asSet(Calendar.class, Date.class));
		registerAllowedTypesForBuiltInConstraint(Pattern.class, CollectionHelper.<Class<?>>asSet(String.class));

		//TODO GM: register all array types
		registerAllowedTypesForBuiltInConstraint(Size.class, CollectionHelper.<Class<?>>asSet(Collection.class, Map.class, String.class, boolean[].class));
	}

	/**
	 * Checks, whether the given type element represents a constraint annotation
	 * or not. That's the case, if the given element is annotated with the
	 * {@link Constraint} meta-annotation (which is only allowed at annotation
	 * declarations).
	 *
	 * @param The
	 *            element of interest.
	 * @return True, if the given element is a constraint annotation type, false
	 *         otherwise.
	 */
	public boolean isConstraintAnnotation(TypeElement typeElement) {
		return typeElement.getAnnotation(Constraint.class) != null;
	}

	public boolean isAnnotationAllowedAtType(DeclaredType annotationType, TypeElement annotatedType) {

		return isAnnotationAllowed(annotationType, annotatedType.asType());
	}

	public boolean isAnnotationAllowedAtMethod(DeclaredType annotationType, ExecutableElement annotatedMethod) {

		return isAnnotationAllowed(annotationType, annotatedMethod.getReturnType());
	}

	public boolean isAnnotationAllowedAtField(DeclaredType annotationType, VariableElement annotatedField) {

		return isAnnotationAllowed(annotationType, annotatedField.asType());
	}

	// ==================================
	// private API below
	// ==================================

	private Set<TypeMirror> getAllSuperTypes(TypeMirror type) {

		Set<TypeMirror> allSuperTypes = CollectionHelper.newHashSet();

		List<? extends TypeMirror> directSupertypes = typeUtils.directSupertypes(type);

		while(!directSupertypes.isEmpty()) {

			for (TypeMirror typeMirror : directSupertypes) {
				allSuperTypes.add(typeMirror);
			}

			List<TypeMirror> nextSuperTypes = CollectionHelper.newArrayList();

			for (TypeMirror typeMirror : directSupertypes) {
				nextSuperTypes.addAll(typeUtils.directSupertypes(typeMirror));
			}
			directSupertypes = nextSuperTypes;
		}


		return allSuperTypes;
	}

	/**
	 * Checks whether the given annotation type may be specified at elements of
	 * the specified type.
	 */
	private boolean isAnnotationAllowed(DeclaredType annotationType, TypeMirror typeOfAnnotatedElement) {

		//TODO GM: implement array type checking
		if(typeOfAnnotatedElement.getKind() == TypeKind.ARRAY) {
			return true;
		}

		//convert primitive types into their corresponding boxed type
		if(typeOfAnnotatedElement.getKind().isPrimitive()) {
			typeOfAnnotatedElement = (DeclaredType)typeUtils.boxedClass((PrimitiveType)typeOfAnnotatedElement).asType();
		}

		Set<TypeMirror> allowedTypesForConstraint = getAllowedTypesForConstraint(annotationType);

		//is the annotation allowed at the given type?
		for (TypeMirror oneAllowedType : allowedTypesForConstraint) {
			if(typeUtils.isAssignable(typeOfAnnotatedElement, oneAllowedType)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns a set with all those type elements, at which the given constraint annotation
	 * type may be specified.
	 *
	 * @param annotationType
	 * @return
	 */
	private Set<TypeMirror> getAllowedTypesForConstraint(DeclaredType annotationType) {

		Set<TypeMirror> theValue;

		if(isBuiltInConstraint(annotationType)) {
			theValue = getAllowedTypesFromBuiltInConstraint(annotationType);
		}
		else {
			theValue = getAllowedTypesFromCustomConstraint(annotationType);
		}

		return theValue;
	}

	private Set<TypeMirror> getAllowedTypesFromBuiltInConstraint(DeclaredType builtInAnnotationType) {

		Set<TypeMirror> theValue = builtInConstraints.get(builtInAnnotationType.asElement().getSimpleName());

		if(theValue == null) {
			theValue = Collections.emptySet();
		}

		return theValue;
	}

	/**
	 * Returns a set containing all those types, at which the specified custom
	 * constraint-annotation is allowed.
	 *
	 * @param customAnnotationType
	 *            A custom constraint type.
	 *
	 * @return A set with all types supported by the given constraint. May be
	 *         empty in case of constraint composition, if there is no common
	 *         type supported by all composing constraints.
	 */
	private Set<TypeMirror> getAllowedTypesFromCustomConstraint(DeclaredType customAnnotationType) {

		Set<TypeMirror> theValue = null;

		//the Constraint meta-annotation at the type declaration, e.g. "@Constraint(validatedBy = CheckCaseValidator.class)"
		AnnotationMirror constraintMetaAnnotation = getConstraintMetaAnnotation(customAnnotationType);

		if(constraintMetaAnnotation == null) {
			throw new IllegalArgumentException("Given type " + customAnnotationType + " isn't a constraint annotation type.");
		}

		//the validator classes, e.g. [CheckCaseValidator.class]
		List<? extends AnnotationValue> validatorClassReferences = getValidatorClassesFromConstraintMetaAnnotation(constraintMetaAnnotation);

		for (AnnotationValue oneValidatorClassReference : validatorClassReferences) {

			if(theValue == null) {
				theValue = CollectionHelper.newHashSet();
			}

			theValue.add(getSupportedType(oneValidatorClassReference));
		}

		Set<AnnotationMirror> composingConstraints = getComposingConstraints(customAnnotationType);

		for (AnnotationMirror oneComposingConstraint : composingConstraints) {

			Set<TypeMirror> allowedTypesForComposingConstraint = getAllowedTypesForConstraint(oneComposingConstraint.getAnnotationType());

			if(theValue == null) {
				theValue = allowedTypesForComposingConstraint;
			}
			else {
				theValue = intersect(theValue, allowedTypesForComposingConstraint);
			}
		}

		if(theValue == null) {
			theValue = Collections.emptySet();
		}

		return theValue;
	}

	private TypeMirror getSupportedType(AnnotationValue oneValidatorClassReference) {

		TypeMirror validatorType = oneValidatorClassReference.accept(new SimpleAnnotationValueVisitor6<TypeMirror, Void>() {

			@Override
			public TypeMirror visitType(TypeMirror t, Void p) {
				return t;
			}
		}, null);

		//contains the bindings of the type parameters from the implemented ConstraintValidator
		//interface, e.g. "ConstraintValidator<CheckCase, String>"
		TypeMirror constraintValidatorImplementation = getConstraintValidatorSuperType(validatorType);


		TypeMirror supportedType = constraintValidatorImplementation.accept(new TypeKindVisitor6<TypeMirror, Void>() {

			@Override
			public TypeMirror visitDeclared(DeclaredType constraintValidatorImplementation, Void p) {
				//2nd type parameter contains the data type supported by current validator class, e.g. "String"
				return constraintValidatorImplementation.getTypeArguments().get(1);
			}

		}, null);

		return supportedType;
	}

	private Set<TypeMirror> intersect(Set<TypeMirror> set1, Set<TypeMirror> set2) {

		Set<TypeMirror> theValue = keepThoseWithSuperTypeAndNoSubType(set1, set2);

		theValue.addAll(keepThoseWithSuperTypeAndNoSubType(set2, set1));

		return theValue;
	}

	//TODO GM: refactor
	private Set<TypeMirror> keepThoseWithSuperTypeAndNoSubType(Set<TypeMirror> set1, Set<TypeMirror> set2) {

		Set<TypeMirror> theValue = CollectionHelper.newHashSet();

		for (TypeMirror oneType : set1) {

			for (TypeMirror typeMirror : set2) {
				if(typeUtils.isSameType(oneType, typeMirror)) {
					theValue.add(oneType);
					continue;
				}
			}

			Set<TypeMirror> superTypes = getAllSuperTypes(oneType);
			for (TypeMirror oneSuperType : superTypes) {
				
				for (TypeMirror typeMirror : set2) {
					if(typeUtils.isSameType(oneSuperType, typeMirror)) {

						if(!containsSubType(oneType, set2)) {
							theValue.add(oneType);
						}
					}
				}
			}
		}

		return theValue;
	}

	private boolean containsSubType(TypeMirror type, Set<TypeMirror> potentialSubTypes) {

		for (TypeMirror onePotentialSubType : potentialSubTypes) {
			
			if(typeUtils.isAssignable(onePotentialSubType, type)) {
				return true;
			}
		}

		return false;
	}

	private TypeMirror getConstraintValidatorSuperType(TypeMirror type) {

		List<? extends TypeMirror> directSupertypes = typeUtils.directSupertypes(type);

		for (TypeMirror typeMirror : directSupertypes) {
			if(typeUtils.asElement(typeMirror).getSimpleName().contentEquals(ConstraintValidator.class.getSimpleName())) {
				return typeMirror;
			}
		}

		throw new AssertionError("Class " + type + " specified in @Constraint.validatedBy doesn't implement ConstraintValidator.");
	}

	/**
	 * Retrieves the {@link Constraint} meta-annotation from the given constraint annotation.
	 *
	 * @param annotationType A constraint type.
	 *
	 * @return The Constraint meta-annotation or null if it isn't specified at the given type.
	 */
	private AnnotationMirror getConstraintMetaAnnotation(DeclaredType annotationType) {

		List<? extends AnnotationMirror> annotationMirrors = annotationType.asElement().getAnnotationMirrors();

		return new AnnotationApiHelper(elementUtils, typeUtils).getMirror(annotationMirrors, Constraint.class);
	}

	private List<? extends AnnotationValue> getValidatorClassesFromConstraintMetaAnnotation(AnnotationMirror constraintMetaAnnotation) {

		Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = constraintMetaAnnotation.getElementValues();

		for (Entry<? extends ExecutableElement, ? extends AnnotationValue> oneElementValue : elementValues.entrySet()) {

			if(oneElementValue.getKey().getSimpleName().contentEquals("validatedBy")) {

				//this is save, as we know that the "validatedBy" attribute is an array of classes
				@SuppressWarnings("unchecked")
				List<? extends AnnotationValue> validatorClasses = (List<? extends AnnotationValue>)oneElementValue.getValue().getValue();

				return validatorClasses;
			}
		}

		return Collections.emptyList();
	}

	private void registerAllowedTypesForBuiltInConstraint(Class<? extends Annotation> annotation, Set<Class<?>> allowedTypes) {

		Set<TypeMirror> allowedTypesAsElements = CollectionHelper.newHashSet();

		for (Class<?> oneType : allowedTypes) {
			if(oneType.isArray()) {
					
				//TODO GM: handle array types
			}
			else {
				TypeElement typeElement = elementUtils.getTypeElement(oneType.getCanonicalName());

				if(typeElement != null) {
					allowedTypesAsElements.add(typeUtils.getDeclaredType(typeElement));
				}
			}
		}

		builtInConstraints.put(elementUtils.getName(annotation.getSimpleName()), allowedTypesAsElements);
	}

	/**
	 * Checks, whether the given type is a built-in constraint annotation (which
	 * is the case, if is annotated with the {@link Constraint} meta-annotation
	 * and is declared in the package <code>javax.validation.constraints</code>).
	 *
	 * @param annotationType
	 *            The type to check.
	 * @return True, if the given type is a constraint annotation, false
	 *         otherwise.
	 */
	private boolean isBuiltInConstraint(DeclaredType annotationType) {

		Element element = annotationType.asElement();

		if(element.getAnnotation(Constraint.class) == null) {
			return false;
		}

		return CONSTRAINT_ANNOTATION_PACKAGE_NAME.equals(elementUtils.getPackageOf(element).getQualifiedName());
	}

	private Set<AnnotationMirror> getComposingConstraints(DeclaredType constraintAnnotationType) {

		Set<AnnotationMirror> theValue = CollectionHelper.newHashSet();

		List<? extends AnnotationMirror> annotationMirrors = constraintAnnotationType.asElement().getAnnotationMirrors();

		for (AnnotationMirror oneAnnotationMirror : annotationMirrors) {
			if(getConstraintMetaAnnotation(oneAnnotationMirror.getAnnotationType()) != null) {
				theValue.add(oneAnnotationMirror);
			}
		}

		return theValue;
	}
	
}