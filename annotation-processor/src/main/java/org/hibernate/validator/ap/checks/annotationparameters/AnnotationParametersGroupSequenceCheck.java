/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.checks.annotationparameters;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.CollectionHelper;

/**
 * Checks that the GroupSequence definition is valid.
 * <ul>
 * <li>the class list contains only interface</li>
 * <li>the defined group sequence is expandable (no cyclic definition)</li>
 * <li>the class list contains the hosting bean class (for default group sequence re-definition)</li>
 * </ul>
 *
 * @author Marko Bekhta
 */
public class AnnotationParametersGroupSequenceCheck extends AnnotationParametersAbstractCheck {

	private Types typeUtils;

	private Elements elementUtils;

	public AnnotationParametersGroupSequenceCheck(AnnotationApiHelper annotationApiHelper, Elements elementUtils, Types typeUtils) {
		super( annotationApiHelper, "javax.validation.GroupSequence" );
		this.typeUtils = typeUtils;
		this.elementUtils = elementUtils;
	}

	@Override
	protected Set<ConstraintCheckIssue> doCheck(Element element, AnnotationMirror annotation) {
		List<? extends AnnotationValue> annotationValue = annotationApiHelper.getAnnotationArrayValue( annotation, "value" );

		TypeElement annotatedElement = (TypeElement) element;

		boolean classForRedefiningGroupSequencePresent = false;

		for ( AnnotationValue value : annotationValue ) {
			// 1. The class list contains only interface
			TypeMirror typeMirror = (TypeMirror) value.getValue();
			boolean isClassAndSameAsAnnotatedElement = isClassForRedefiningGroupSequence( annotatedElement, typeMirror );

			if ( !annotationApiHelper.isInterface( typeMirror ) && !isClassAndSameAsAnnotatedElement ) {
				return CollectionHelper.asSet( ConstraintCheckIssue.error(
						element, annotation, "INVALID_GROUP_SEQUENCE_VALUE_NOT_INTERFACES_ANNOTATION_PARAMETERS"
				) );
			}
			// 2. the defined group sequence is expandable (no cyclic definition)
			if ( isFromHierarchy( annotatedElement, typeMirror ) && !isClassAndSameAsAnnotatedElement ) {
				return CollectionHelper.asSet( ConstraintCheckIssue.error(
						element, annotation, "INVALID_GROUP_SEQUENCE_VALUE_CYCLIC_DEFINITION_ANNOTATION_PARAMETERS"
				) );
			}
			// 3. the class list contains the hosting bean class (for default group sequence re-definition)
			classForRedefiningGroupSequencePresent = classForRedefiningGroupSequencePresent || isClassAndSameAsAnnotatedElement;
		}

		// if the annotated element is a class and does not contain itself in a group then it's bad group redefinition
		if ( ElementKind.CLASS.equals( annotatedElement.getKind() ) && !classForRedefiningGroupSequencePresent ) {
			return CollectionHelper.asSet( ConstraintCheckIssue.error(
					element, annotation, "INVALID_GROUP_SEQUENCE_VALUE_HOSTING_BEAN_ANNOTATION_PARAMETERS"
			) );
		}

		return Collections.emptySet();
	}

	/**
	 * Checks if given element ({@link TypeElement}) is present somewhere in inheritance hierarchy of given type mirror ({@link TypeMirror}).
	 *
	 * @param annotatedElement an element to check
	 * @param typeMirror an mirror
	 *
	 * @return
	 */
	private boolean isFromHierarchy(TypeElement annotatedElement, TypeMirror typeMirror) {

		while ( annotatedElement != null && !"java.lang.Object".equals( annotatedElement.getQualifiedName().toString() ) ) {
			if ( typeUtils.isSameType( annotatedElement.asType(), typeMirror ) ) {
				return true;
			}

			for ( TypeMirror interfaceMirror : annotatedElement.getInterfaces() ) {
				if ( typeUtils.isSubtype( interfaceMirror, typeMirror ) ) {
					return true;
				}
			}
			annotatedElement = (TypeElement) typeUtils.asElement( annotatedElement.getSuperclass() );
		}

		return false;
	}

	/**
	 * Checks if a given {@link TypeMirror} is not a class used for redefining a group sequence.
	 *
	 * @param annotatedElement an element on which a redefined group sequence annotation is present
	 * @param typeMirror a type mirror to check
	 *
	 * @return {@code true} if the given annotated element is class and is the same type as provided
	 * type mirror
	 */
	private boolean isClassForRedefiningGroupSequence(TypeElement annotatedElement, TypeMirror typeMirror) {
		return ElementKind.CLASS.equals( annotatedElement.getKind() ) && typeUtils.isSameType( annotatedElement.asType(), typeMirror );
	}

}
