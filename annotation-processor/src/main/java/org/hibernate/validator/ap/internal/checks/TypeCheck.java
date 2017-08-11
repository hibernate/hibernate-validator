/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.checks;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.internal.util.AnnotationApiHelper;
import org.hibernate.validator.ap.internal.util.AnnotationApiHelper.UnwrapMode;
import org.hibernate.validator.ap.internal.util.CollectionHelper;
import org.hibernate.validator.ap.internal.util.ConstraintHelper;
import org.hibernate.validator.ap.internal.util.ConstraintHelper.AnnotationProcessorValidationTarget;
import org.hibernate.validator.ap.internal.util.ConstraintHelper.ConstraintCheckResult;

/**
 * Checks, that constraint annotations are only specified at elements of a type supported by the constraints. Applies to
 * fields, methods and non-annotation type declarations.
 *
 * @author Gunnar Morling
 * @author Marko Bekhta
 */
public class TypeCheck extends AbstractConstraintCheck {

	private final ConstraintHelper constraintHelper;

	private final Types typeUtils;

	private final AnnotationApiHelper annotationApiHelper;

	public TypeCheck(ConstraintHelper constraintHelper, Types typeUtils, AnnotationApiHelper annotationApiHelper) {
		this.constraintHelper = constraintHelper;
		this.typeUtils = typeUtils;
		this.annotationApiHelper = annotationApiHelper;
	}

	@Override
	public Set<ConstraintCheckIssue> checkField(VariableElement element, AnnotationMirror annotation) {
		return checkInternal( element, annotation, element.asType(), "NOT_SUPPORTED_TYPE" );
	}

	@Override
	public Set<ConstraintCheckIssue> checkMethod(ExecutableElement element, AnnotationMirror annotation) {
		AnnotationProcessorValidationTarget target = AnnotationProcessorValidationTarget.ANNOTATED_ELEMENT;
		if ( constraintHelper.isConstraintAnnotation( annotation.getAnnotationType().asElement() ) ) {
			target = constraintHelper.resolveValidationTarget( element, annotation );
		}

		if ( target == AnnotationProcessorValidationTarget.PARAMETERS ) {
			return Collections.emptySet();
		}

		// check the return type
		return checkInternal( element, annotation, element.getReturnType(), "NOT_SUPPORTED_RETURN_TYPE" );
	}

	@Override
	public Set<ConstraintCheckIssue> checkNonAnnotationType(TypeElement element, AnnotationMirror annotation) {
		return checkInternal( element, annotation, element.asType(), "NOT_SUPPORTED_TYPE" );
	}

	private Set<ConstraintCheckIssue> checkInternal(Element element, AnnotationMirror annotation, TypeMirror type, String messageKey) {
		Optional<TypeMirror> typeToCheck = usesUnwrapping( annotation, type ) ?
				getUnwrappedType( type ) :
				Optional.of( type );
		if ( typeToCheck.isPresent() ) {
			if ( !isAnnotationAllowedForType( annotation, typeToCheck.get() ) ) {
				return CollectionHelper.asSet(
						ConstraintCheckIssue.error(
								element, annotation, messageKey,
								annotation.getAnnotationType().asElement().getSimpleName()
						) );
			}
		}
		else {
			// it means that the type was marked for unwrapping but unwrapped value type was not found
			return CollectionHelper.asSet(
					ConstraintCheckIssue.warning(
							element, annotation, "NOT_FOUND_UNWRAPPED_TYPE",
							annotation.getAnnotationType().asElement().getSimpleName()
					) );
		}

		return Collections.emptySet();
	}

	private boolean isAnnotationAllowedForType(AnnotationMirror annotation, TypeMirror type) {
		return ConstraintCheckResult.ALLOWED.equals( constraintHelper.checkConstraint( annotation.getAnnotationType(), type ) );
	}

	private boolean usesUnwrapping(AnnotationMirror annotationMirror, TypeMirror typeMirror) {
		UnwrapMode mode = annotationApiHelper.determineUnwrapMode( annotationMirror );
		if ( UnwrapMode.SKIP.equals( mode ) ) {
			return false;
		}

		//need to check if this annotation is not on one of the types that are automatically unwrapped:
		if ( constraintHelper.isSupportedForUnwrappingByDefault( getQualifiedName( typeMirror ) ) ) {
			return true;
		}

		//otherwise look for Unwrapping type in payload:
		return UnwrapMode.UNWRAP.equals( mode );
	}

	private Optional<TypeMirror> getUnwrappedType(TypeMirror type) {
		return constraintHelper.getUnwrappedToByDefault( getQualifiedName( type ) );
	}

	private Name getQualifiedName(TypeMirror typeMirror) {
		if ( TypeKind.DECLARED.equals( typeMirror.getKind() ) ) {
			return ( (TypeElement) typeUtils.asElement( typeMirror ) ).getQualifiedName();
		}
		else {
			return null;
		}
	}
}
