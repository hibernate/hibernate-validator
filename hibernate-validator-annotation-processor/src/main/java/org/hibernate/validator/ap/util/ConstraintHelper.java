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
import java.util.Collection;
import java.util.Collections;
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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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

	private static Map<Name, Set<TypeElement>> builtInConstraints;

	private Elements elementUtils;

	private Types typeUtils;

	public ConstraintHelper(Elements elementUtils, Types typeUtils) {

		this.elementUtils = elementUtils;
		this.typeUtils = typeUtils;

		CONSTRAINT_ANNOTATION_PACKAGE_NAME = elementUtils.getName(Size.class.getPackage().getName());

		builtInConstraints = CollectionHelper.newHashMap();

		//TODO GM: register all types
		registerAllowedTypesForBuiltInConstraint(Size.class, CollectionHelper.<Class<?>>asSet(Collection.class, String.class));
		registerAllowedTypesForBuiltInConstraint(AssertTrue.class, CollectionHelper.<Class<?>>asSet(Boolean.class, boolean.class));
		registerAllowedTypesForBuiltInConstraint(NotNull.class, CollectionHelper.<Class<?>>asSet(Object.class));
		registerAllowedTypesForBuiltInConstraint(Min.class, CollectionHelper.<Class<?>>asSet(Integer.class, Long.class));
	}

	/**
	 * Checks, whether the given type element represents a constraint annotation
	 * or not. That's the case, if the given element is annotated with the
	 * {@link Constraint} meta-annotation (which is only allowed at annotation
	 * declarations).
	 *
	 * @param typeElement The element of interest.
	 * @return True, if the given element is a constraint annotation type, false
	 *         otherwise.
	 */
	public boolean isConstraintAnnotation(TypeElement typeElement) {
		return typeElement.getAnnotation(Constraint.class) != null;
	}

	public boolean isAnnotationAllowedAtElement(DeclaredType annotationType, Element annotatedElement) {

		Set<TypeElement> allowedTypesForConstraint = getAllowedTypesForConstraint(annotationType);

		if(allowedTypesForConstraint.isEmpty()) {
			return false;
		}

		Element typeElementOfAnnotatedElement = typeUtils.asElement(annotatedElement.asType());


		if(allowedTypesForConstraint.contains(typeElementOfAnnotatedElement)) {
			return true;
		}

		List<? extends TypeMirror> directSupertypes = typeUtils.directSupertypes(annotatedElement.asType());

		while(!directSupertypes.isEmpty()) {
			List<TypeMirror> nextSupertypes = CollectionHelper.newArrayList();
			for (TypeMirror oneSuperType : directSupertypes) {

				Element oneSuperTypeAsElement = typeUtils.asElement(oneSuperType);

				if(allowedTypesForConstraint.contains(oneSuperTypeAsElement)) {
					return true;
				}

				nextSupertypes.addAll(typeUtils.directSupertypes(oneSuperType));
			}

			directSupertypes = nextSupertypes;
		}


		return false;
	}

	// ==================================
	// private API below
	// ==================================

	/**
	 * Returns a set with all those type elements, at which the given constraint annotation
	 * type may be specified.
	 *
	 * @param annotationType
	 * @return
	 */
	private Set<TypeElement> getAllowedTypesForConstraint(DeclaredType annotationType) {

		if(isBuiltInConstraint(annotationType)) {
			return getAllowedTypesFromBuiltInConstraint(annotationType);
		}
		else {
			return getAllowedTypesFromCustomConstraint(annotationType);
		}
	}

	private Set<TypeElement> getAllowedTypesFromBuiltInConstraint(DeclaredType builtInAnnotationType) {

		Set<TypeElement> theValue = builtInConstraints.get(builtInAnnotationType.asElement().getSimpleName());

		if(theValue == null) {
			theValue = Collections.emptySet();
		}

		return theValue;
	}

	/**
	 * Returns a set containing all those types, at which the specified custom
	 * constraint-annotation is allowed.
	 *
	 * @param customInAnnotationType
	 *            A custom constraint type.
	 *
	 * @return A set with all types supported by the given constraint. May be
	 *         empty in case of constraint composition, if there is no common
	 *         type supported by all composing constraints.
	 *
	 *         TODO GM: consider constraint composition
	 */
	private Set<TypeElement> getAllowedTypesFromCustomConstraint(DeclaredType customInAnnotationType) {

		Set<TypeElement> theValue = CollectionHelper.newHashSet();

		//the Constraint meta-annotation at the type declaration, e.g. "@Constraint(validatedBy = CheckCaseValidator.class)"
		AnnotationMirror constraintMetaAnnotation = getConstraintMetaAnnotation(customInAnnotationType);

		if(constraintMetaAnnotation == null) {
			return theValue;
		}

		//the validator classes, e.g. [CheckCaseValidator.class]
		List<? extends AnnotationValue> validatorClassReferences = getValidatorClassesFromConstraintMetaAnnotation(constraintMetaAnnotation);

		for (AnnotationValue oneValidatorClassReference : validatorClassReferences) {

			DeclaredType validatorType = (DeclaredType)oneValidatorClassReference.getValue();

			//contains the bindings of the type parameters from the implemented ConstraintValidator
			//interface, e.g. "ConstraintValidator<CheckCase, String>"
			DeclaredType constraintValidatorImplementation = getConstraintValidatorSuperType(validatorType);

			if(constraintValidatorImplementation != null) {

				//2nd type parameter contains the data type supported by current validator class, e.g. "String"
				TypeMirror supportedTypeParameter = constraintValidatorImplementation.getTypeArguments().get(1);
				theValue.add((TypeElement)typeUtils.asElement(supportedTypeParameter));
			}
		}

		return theValue;
	}

	private DeclaredType getConstraintValidatorSuperType(DeclaredType type) {

		List<? extends TypeMirror> directSupertypes = typeUtils.directSupertypes(type);

		for (TypeMirror typeMirror : directSupertypes) {
			if(typeUtils.asElement(typeMirror).getSimpleName().contentEquals(ConstraintValidator.class.getSimpleName())) {
				return (DeclaredType)typeMirror;
			}
		}

		return null;
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

		Set<TypeElement> allowedTypesAsElements = CollectionHelper.newHashSet();

		for (Class<?> oneType : allowedTypes) {
			TypeElement typeElement = elementUtils.getTypeElement(oneType.getCanonicalName());

			if(typeElement != null) {
				allowedTypesAsElements.add(typeElement);
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
}