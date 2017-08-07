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
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.internal.util.AnnotationApiHelper;
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
		TypeMirror typeToCheck = usesUnwrapping( annotation, type ) ? getUnwrappedType( type ) : type;
		if ( !isAnnotationAllowedForType( annotation, typeToCheck ) ) {
			return CollectionHelper.asSet(
					ConstraintCheckIssue.error(
							element, annotation, messageKey,
							annotation.getAnnotationType().asElement().getSimpleName()
					) );
		}

		return Collections.emptySet();
	}

	private boolean isAnnotationAllowedForType(AnnotationMirror annotation, TypeMirror type) {
		return ConstraintCheckResult.ALLOWED.equals( constraintHelper.checkConstraint( annotation.getAnnotationType(), type ) );
	}

	private boolean usesUnwrapping(AnnotationMirror annotationMirror, TypeMirror typeMirror) {
		//need to check if this annotation is not on one of the types that are automatically unwrapped:
		if ( constraintHelper.isSupportedForUnwrappingByDefault( getQualifiedName( typeMirror ) ) ) {
			return true;
		}

		//otherwise look for Unwrapping type in payload:
		return annotationApiHelper.getAnnotationArrayValue( annotationMirror, "payload" ).stream()
				.map( AnnotationValue::getValue )
				.map( type -> (TypeMirror) type )
				.map( typeUtils::asElement )
				.map( elem -> ( (TypeElement) elem ).getQualifiedName() )
				.filter( name -> name.toString().equals( "javax.validation.valueextraction.Unwrapping.Unwrap" ) )
				.findAny().isPresent();
	}

	private TypeMirror getUnwrappedType(TypeMirror type) {
		Optional<TypeMirror> optional = constraintHelper.getUnwrappedToByDefault( getQualifiedName( type ) );
		if ( optional.isPresent() ) {
			return optional.get();
		}
		else {
			//TODO: need to find a way to check for unwrapping
			return type;
		}
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
