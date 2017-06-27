/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.checks.annotationparameters;

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

import org.hibernate.validator.ap.internal.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.internal.util.AnnotationApiHelper;
import org.hibernate.validator.ap.internal.util.CollectionHelper;
import org.hibernate.validator.ap.internal.util.ConstraintHelper;
import org.hibernate.validator.ap.internal.util.TypeNames;

/**
 * Checks that the GroupSequence definition is valid.
 * <ul>
 * <li>the class list contains only interfaces (except for the hosting bean in the case of default group sequence redefinition)</li>
 * <li>the defined group sequence is expandable (no cyclic definition)</li>
 * <li>the group sequence does not extend other interfaces</li>
 * </ul>
 *
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public class GroupSequenceCheck extends AnnotationParametersAbstractCheck {

	private final Types typeUtils;
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

		boolean isDefaultGroupSequenceRedefinition = false;

		Set<String> qualifiedNames = CollectionHelper.newHashSet();

		Set<ConstraintCheckIssue> issues = CollectionHelper.newHashSet();

		for ( AnnotationValue value : annotationValue ) {
			TypeMirror typeMirror = (TypeMirror) value.getValue();

			// 1. if it is a class, it has to be the hosting bean class (for default group sequence redefinition)
			if ( annotationApiHelper.isClass( typeMirror ) && redefinesDefaultGroupSequence( annotatedElement, typeMirror ) ) {
				isDefaultGroupSequenceRedefinition = true;
			}
			else if ( !annotationApiHelper.isInterface( typeMirror ) ) {
				issues.add( ConstraintCheckIssue.error(
						element, annotation, "INVALID_GROUP_SEQUENCE_VALUE_NOT_INTERFACES"
				) );
				// it is not an interface or a class used to redefine the default group, we will not do any other
				// checks on it and report the error straight away
				continue;
			}

			// 2. an interface should not be declared multiple times in a group sequence
			String qualifiedName = ( (TypeElement) typeUtils.asElement( typeMirror ) ).getQualifiedName().toString();
			if ( qualifiedNames.contains( qualifiedName ) ) {
				issues.add( ConstraintCheckIssue.error(
						element, annotation, "INVALID_GROUP_SEQUENCE_VALUE_MULTIPLE_DECLARATIONS_OF_THE_SAME_INTERFACE", qualifiedName )
				);
			}
			qualifiedNames.add( qualifiedName );
		}

		// 3. if the group sequence extends other interfaces we need to produce a warning
		if ( ElementKind.INTERFACE.equals( annotatedElement.getKind() ) && !annotatedElement.getInterfaces().isEmpty() ) {
			issues.add( ConstraintCheckIssue.warning(
					element,
					annotation,
					"INVALID_GROUP_SEQUENCE_EXTEND_INTERFACES"
			) );
		}

		// 4. if the annotated element is a class and the group sequence does not contain it then the group sequence redefinition is invalid
		if ( ElementKind.CLASS.equals( annotatedElement.getKind() ) && !isDefaultGroupSequenceRedefinition ) {
			issues.add( ConstraintCheckIssue.error( element, annotation, "INVALID_GROUP_SEQUENCE_VALUE_MISSING_HOSTING_BEAN_DECLARATION" ) );
		}

		// 5. the defined group sequence is expandable (no cyclic definition)
		ConstraintCheckIssue cyclicDefinitionIssue = checkForCyclicDefinition( CollectionHelper.newHashSet(), annotatedElement.asType(), annotatedElement, annotation );
		if ( cyclicDefinitionIssue != null ) {
			issues.add( cyclicDefinitionIssue );
		}

		return issues;
	}

	/**
	 * Checks if there are any cyclic definitions for a given type element {@link TypeElement}.
	 *
	 * @param processedTypes for initial call of this functions this can be an empty {@link Set}. It contains all
	 * already processed types. Used for recursion
	 * @param currentTypeMirror the current type mirror under investigation
	 * @param originalElement an original annotated element to be used in reported error if there is one
	 * @param annotation an original annotation mirror passed to a check method to be used in reported error if there is
	 * one
	 * @return {@code null} if there was no cyclic issue found, {@link ConstraintCheckIssue} describing a cyclic issue
	 * if one was found.
	 */
	private ConstraintCheckIssue checkForCyclicDefinition(Set<TypeMirror> processedTypes, TypeMirror currentTypeMirror,
			TypeElement originalElement, AnnotationMirror annotation) {
		// if the passed currentTypeMirror is not a class/interface then we simply ignore it as such errors should have
		// already been processed earlier.
		if ( !TypeKind.DECLARED.equals( currentTypeMirror.getKind() ) ) {
			return null;
		}
		if ( processedTypes.contains( currentTypeMirror ) ) {
			if ( !redefinesDefaultGroupSequence( originalElement, currentTypeMirror ) ) {
				return ConstraintCheckIssue.error( originalElement, annotation, "INVALID_GROUP_SEQUENCE_VALUE_CYCLIC_DEFINITION" );
			}
			else {
				return null;
			}
		}
		else {
			processedTypes.add( currentTypeMirror );
			// check if there is a @GroupSequence annotation on a currentTypeMirror and check its values if present
			List<? extends AnnotationValue> annotationArrayValue = annotationApiHelper.getAnnotationArrayValue( getGroupSequence( currentTypeMirror ), "value" );
			if ( annotationArrayValue != null ) {
				for ( AnnotationValue value : annotationArrayValue ) {
					TypeMirror groupTypeMirror = (TypeMirror) value.getValue();
					ConstraintCheckIssue issue = checkForCyclicDefinition( processedTypes, groupTypeMirror, originalElement, annotation );
					if ( issue != null ) {
						return issue;
					}
				}
			}
			// check if currentTypeMirror extends any other interfaces and if so check if they are not in the sequence already
			for ( TypeMirror extendedInterfaceTypeMirror : ( (TypeElement) typeUtils.asElement( currentTypeMirror ) ).getInterfaces() ) {
				ConstraintCheckIssue issue = checkForCyclicDefinition( processedTypes, extendedInterfaceTypeMirror, originalElement, annotation );
				if ( issue != null ) {
					return issue;
				}
			}
		}
		return null;
	}

	/**
	 * Find a {@code javax.validation.GroupSequence} annotation if one is present on given type ({@link TypeMirror}).
	 */
	private AnnotationMirror getGroupSequence(TypeMirror typeMirror) {
		// the annotation can be present only on TypeKind.DECLARED elements
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
	 * Check if the given {@link TypeMirror} redefines the default group sequence for the annotated class.
	 * <p>
	 * Note that it is only the case if the annotated element is a class.
	 */
	private boolean redefinesDefaultGroupSequence(TypeElement annotatedElement, TypeMirror typeMirror) {
		return ElementKind.CLASS.equals( annotatedElement.getKind() ) && typeUtils.isSameType( annotatedElement.asType(), typeMirror );
	}

}
