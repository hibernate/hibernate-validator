/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.checks.annotationparameters;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.CollectionHelper;
import org.hibernate.validator.ap.util.ConstraintHelper;
import org.hibernate.validator.ap.util.TypeNames;

/**
 * Checks that the GroupSequence definition is valid.
 * <ul>
 * <li>the class list contains only interface</li>
 * <li>the defined group sequence is expandable (no cyclic definition)</li>
 * <li>the class list contains the hosting bean class (for default group sequence re-definition)</li>
 * <li>the class list does not use interfaces that extend others</li>
 * </ul>
 *
 * @author Marko Bekhta
 */
public class GroupSequenceCheck extends AnnotationParametersAbstractCheck {

	private Types typeUtils;
	private final ConstraintHelper constraintHelper;

	public GroupSequenceCheck(AnnotationApiHelper annotationApiHelper, Types typeUtils, ConstraintHelper constraintHelper) {
		super( annotationApiHelper, TypeNames.BeanValidationTypes.GROUP_SEQUENCE );
		this.typeUtils = typeUtils;
		this.constraintHelper = constraintHelper;
	}

	@Override
	protected Set<ConstraintCheckIssue> doCheck(Element element, AnnotationMirror annotation) {
		List<? extends AnnotationValue> annotationValue = annotationApiHelper.getAnnotationArrayValue( annotation, "value" );

		TypeElement annotatedElement = (TypeElement) element;

		boolean classForRedefiningGroupSequencePresent = false;

		Set<String> qualifiedNames = CollectionHelper.newHashSet();

		Set<ConstraintCheckIssue> issues = CollectionHelper.newHashSet();

		for ( AnnotationValue value : annotationValue ) {
			// 1. The class list contains only interface
			TypeMirror typeMirror = (TypeMirror) value.getValue();
			boolean isClassAndSameAsAnnotatedElement = isClassForRedefiningGroupSequence( annotatedElement, typeMirror );

			if ( !annotationApiHelper.isInterface( typeMirror ) && !isClassAndSameAsAnnotatedElement ) {
				issues.add( ConstraintCheckIssue.error(
						element, annotation, "INVALID_GROUP_SEQUENCE_VALUE_NOT_INTERFACES_ANNOTATION_PARAMETERS"
				) );
				// if it's not an interface we will not do any other checks on it
				continue;
			}
			// 3. the class list contains the hosting bean class (for default group sequence re-definition)
			classForRedefiningGroupSequencePresent = classForRedefiningGroupSequencePresent || isClassAndSameAsAnnotatedElement;
			// 4. one interface should not be declared multiple times in a group sequence
			String qualifiedName = ( (TypeElement) typeUtils.asElement( typeMirror ) ).getQualifiedName().toString();
			if ( qualifiedNames.contains( qualifiedName ) ) {
				issues.add( ConstraintCheckIssue.error(
						element, annotation, "INVALID_GROUP_SEQUENCE_VALUE_MULTIPLE_DECLARATIONS_ANNOTATION_PARAMETERS", qualifiedName )
				);
			}
			// 5. if interface extends other interfaces we need to produce a warning
			if ( annotationApiHelper.isInterface( typeMirror ) && !( (TypeElement) typeUtils.asElement( typeMirror ) ).getInterfaces().isEmpty() ) {
				issues.add( ConstraintCheckIssue.warning(
						element,
						annotation,
						"INVALID_GROUP_SEQUENCE_VALUE_EXTENDED_INTERFACES_ANNOTATION_PARAMETERS",
						qualifiedName
				) );
			}
			qualifiedNames.add( qualifiedName );
		}

		// 2. the defined group sequence is expandable (no cyclic definition)
		ConstraintCheckIssue cyclicIssue = checkForCyclicDefinition( CollectionHelper.<TypeMirror>newHashSet(), annotatedElement.asType(), annotatedElement, annotation );
		if ( cyclicIssue != null ) {
			issues.add( cyclicIssue );
		}

		// if the annotated element is a class and does not contain itself in a group then it's bad group redefinition
		if ( ElementKind.CLASS.equals( annotatedElement.getKind() ) && !classForRedefiningGroupSequencePresent ) {
			issues.add( ConstraintCheckIssue.error(
					element, annotation, "INVALID_GROUP_SEQUENCE_VALUE_HOSTING_BEAN_ANNOTATION_PARAMETERS"
			) );
		}

		return issues;
	}

	/**
	 * Checks if there are any cyclic definitions for a given type element {@link TypeElement}.
	 *
	 * @param processedTypes for initial call of this functions this can be an empty {@link Set}. It contains all already processed types. Used for recursion
	 * @param typeMirror a current type mirror under investigation
	 * @param originalElement an original annotated element to be used in reported error if there's one
	 * @param annotation an original annotation mirror passed to a check method to be used in reported error if there's one
	 *
	 * @return {@code null} if there was no cyclic issue found, {@link ConstraintCheckIssue} describing a cyclic issue if one was found.
	 */
	private ConstraintCheckIssue checkForCyclicDefinition(Set<TypeMirror> processedTypes, TypeMirror typeMirror, TypeElement originalElement, AnnotationMirror annotation) {
		if ( processedTypes.contains( typeMirror ) ) {
			if ( !isClassForRedefiningGroupSequence( originalElement, typeMirror ) ) {
				return ConstraintCheckIssue.error( originalElement, annotation, "INVALID_GROUP_SEQUENCE_VALUE_CYCLIC_DEFINITION_ANNOTATION_PARAMETERS" );
			}
			else {
				return null;
			}
		}
		else {
			processedTypes.add( typeMirror );

			AnnotationMirror groupSequenceMirror = getGroupSequence( typeMirror );
			List<? extends AnnotationValue> annotationValue = annotationApiHelper.getAnnotationArrayValue( groupSequenceMirror, "value" );
			if ( annotationValue != null ) {
				for ( AnnotationValue value : annotationValue ) {
					TypeMirror groupTypeMirror = (TypeMirror) value.getValue();
					ConstraintCheckIssue issue = checkForCyclicDefinition( processedTypes, groupTypeMirror, originalElement, annotation );
					if ( issue != null ) {
						return issue;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Finds a {@code javax.validation.GroupSequence} annotation if one is present on given type ({@link TypeMirror}.
	 *
	 * @param typeMirror a type on which to check for {@code javax.validation.GroupSequence} annotation
	 *
	 * @return {@link AnnotationMirror} that represents a {@code javax.validation.GroupSequence} if one was present on given type,
	 * {@code null} otherwise
	 */
	private AnnotationMirror getGroupSequence(TypeMirror typeMirror) {
		// other annotations can be present only on TypeKind.DECLARED things so need to check to prevent NPE
		if ( TypeKind.DECLARED.equals( typeMirror.getKind() ) ) {
			for ( AnnotationMirror annotationMirror : typeUtils.asElement( typeMirror ).getAnnotationMirrors() ) {
				if ( ConstraintHelper.AnnotationType.GROUP_SEQUENCE_ANNOTATION.equals( constraintHelper.getAnnotationType( annotationMirror ) ) ) {
					return annotationMirror;
				}
			}
		}
		return null;
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
