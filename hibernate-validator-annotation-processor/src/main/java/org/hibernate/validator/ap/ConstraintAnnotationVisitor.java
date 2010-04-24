// $Id$
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
package org.hibernate.validator.ap;

import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor6;

import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.ConstraintHelper;
import org.hibernate.validator.ap.util.ConstraintHelper.ConstraintCheckResult;
import org.hibernate.validator.ap.util.MessagerAdapter;

/**
 * An {@link ElementVisitor} that visits elements (type declarations, methods
 * and fields) annotated with constraint annotations from the Bean Validation
 * API.
 *
 * @author Gunnar Morling.
 */
final class ConstraintAnnotationVisitor extends ElementKindVisitor6<Void, List<AnnotationMirror>> {

	private final ConstraintHelper constraintHelper;

	private final MessagerAdapter messager;

	public ConstraintAnnotationVisitor(ProcessingEnvironment processingEnvironment, MessagerAdapter messager) {

		this.messager = messager;

		AnnotationApiHelper annotationApiHelper = new AnnotationApiHelper(
				processingEnvironment.getElementUtils(), processingEnvironment.getTypeUtils()
		);

		constraintHelper = new ConstraintHelper(
				processingEnvironment.getElementUtils(), processingEnvironment.getTypeUtils(), annotationApiHelper
		);
	}

	/**
	 * <p>
	 * Checks whether the given mirrors representing one or more constraint annotations are correctly
	 * specified at the given method. The following checks are performed:</p>
	 * <ul>
	 * <li>
	 * The method must be a JavaBeans getter method (name starts with "is", "get" or "has",
	 * method has return type, but no parameters).
	 * </li>
	 * <li>
	 * The return type of the method must be supported by the constraints.
	 * </li>
	 * <li>
	 * The method must not be static.
	 * </li>
	 * </ul>
	 */
	@Override
	public Void visitExecutableAsMethod(ExecutableElement method,
										List<AnnotationMirror> mirrors) {

		for ( AnnotationMirror oneAnnotationMirror : mirrors ) {


			switch ( constraintHelper.getAnnotationType( oneAnnotationMirror ) ) {

				case CONSTRAINT_ANNOTATION:
					checkConstraintAtMethod( method, oneAnnotationMirror );
					break;

				case MULTI_VALUED_CONSTRAINT_ANNOTATION:
					for ( AnnotationMirror onePartOfMultiValuedConstraint :
							constraintHelper.getPartsOfMultiValuedConstraint( oneAnnotationMirror ) ) {

						checkConstraintAtMethod( method, onePartOfMultiValuedConstraint );
					}
					break;

				case GRAPH_VALIDATION_ANNOTATION:
					checkGraphValidationAnnotationAtMethod( method, oneAnnotationMirror );
			}

		}

		return null;
	}

	/**
	 * <p>
	 * Checks whether the given mirrors representing one or more constraint annotations are correctly
	 * specified at the given field. The following checks are performed:</p>
	 * <ul>
	 * <li>
	 * The type of the field must be supported by the constraints.
	 * </li>
	 * <li>
	 * The field must not be static.
	 * </li>
	 * </ul>
	 */
	@Override
	public Void visitVariableAsField(VariableElement annotatedField, List<AnnotationMirror> mirrors) {

		for ( AnnotationMirror oneAnnotationMirror : mirrors ) {

			switch ( constraintHelper.getAnnotationType( oneAnnotationMirror ) ) {

				case CONSTRAINT_ANNOTATION:
					checkConstraintAtField( annotatedField, oneAnnotationMirror );
					break;

				case MULTI_VALUED_CONSTRAINT_ANNOTATION:
					for ( AnnotationMirror onePartOfMultiValuedConstraint :
							constraintHelper.getPartsOfMultiValuedConstraint( oneAnnotationMirror ) ) {

						checkConstraintAtField( annotatedField, onePartOfMultiValuedConstraint );
					}
					break;

				case GRAPH_VALIDATION_ANNOTATION:
					checkGraphValidationAnnotationAtField( annotatedField, oneAnnotationMirror );
			}
		}

		return null;
	}

	/**
	 * <p>
	 * Checks whether the given mirrors representing one or more constraint
	 * annotations are correctly specified at the given annotation type
	 * declaration. The only annotation types allowed to be annotated with a
	 * constraint annotation are other (composed) constraint annotation type
	 * declarations.
	 * </p>
	 */
	@Override
	public Void visitTypeAsAnnotationType(TypeElement annotationType,
										  List<AnnotationMirror> mirrors) {

		for ( AnnotationMirror oneAnnotationMirror : mirrors ) {

			switch ( constraintHelper.getAnnotationType( oneAnnotationMirror ) ) {

				case CONSTRAINT_ANNOTATION:
					checkConstraintAtAnnotationType( annotationType, oneAnnotationMirror );
					break;

				case MULTI_VALUED_CONSTRAINT_ANNOTATION:
					for ( AnnotationMirror onePartOfMultiValuedConstraint :
							constraintHelper.getPartsOfMultiValuedConstraint( oneAnnotationMirror ) ) {

						checkConstraintAtAnnotationType( annotationType, onePartOfMultiValuedConstraint );
					}
					break;
			}
		}

		return null;
	}

	@Override
	public Void visitTypeAsClass(TypeElement e, List<AnnotationMirror> p) {
		return visitClassOrInterfaceOrEnumType( e, p );
	}

	@Override
	public Void visitTypeAsEnum(TypeElement e, List<AnnotationMirror> p) {
		return visitClassOrInterfaceOrEnumType( e, p );
	}

	@Override
	public Void visitTypeAsInterface(TypeElement e, List<AnnotationMirror> p) {
		return visitClassOrInterfaceOrEnumType( e, p );
	}

	// ==================================
	// private API below
	// ==================================

	private Void visitClassOrInterfaceOrEnumType(TypeElement annotatedType,
												 List<AnnotationMirror> mirrors) {

		for ( AnnotationMirror oneAnnotationMirror : mirrors ) {

			switch ( constraintHelper.getAnnotationType( oneAnnotationMirror ) ) {

				case CONSTRAINT_ANNOTATION:
					checkConstraintAtType( annotatedType, oneAnnotationMirror );
					break;

				case MULTI_VALUED_CONSTRAINT_ANNOTATION:
					for ( AnnotationMirror onePartOfMultiValuedConstraint :
							constraintHelper.getPartsOfMultiValuedConstraint( oneAnnotationMirror ) ) {
						checkConstraintAtType( annotatedType, onePartOfMultiValuedConstraint );
					}
					break;
			}

		}

		return null;
	}

	private void checkConstraintAtType(TypeElement annotatedType, AnnotationMirror mirror) {

		if ( constraintHelper.checkConstraint(
				mirror.getAnnotationType(), annotatedType.asType()
		) != ConstraintCheckResult.ALLOWED ) {

			messager.reportError(
					annotatedType, mirror, "NOT_SUPPORTED_TYPE",
					mirror.getAnnotationType().asElement().getSimpleName()
			);
		}
	}

	private void checkConstraintAtField(VariableElement annotatedField, AnnotationMirror annotationMirror) {

		if ( isStaticElement( annotatedField ) ) {

			messager.reportError( annotatedField, annotationMirror, "STATIC_FIELDS_MAY_NOT_BE_ANNOTATED" );

			return;
		}

		if ( constraintHelper.checkConstraint(
				annotationMirror.getAnnotationType(), annotatedField.asType()
		) != ConstraintCheckResult.ALLOWED ) {

			messager.reportError(
					annotatedField, annotationMirror, "NOT_SUPPORTED_TYPE",
					annotationMirror.getAnnotationType().asElement().getSimpleName()
			);
		}
	}

	private void checkConstraintAtMethod(ExecutableElement method, AnnotationMirror mirror) {

		if ( !isGetterMethod( method ) ) {

			messager.reportError( method, mirror, "ONLY_GETTERS_MAY_BE_ANNOTATED" );

			return;
		}

		if ( isStaticElement( method ) ) {

			messager.reportError( method, mirror, "STATIC_METHODS_MAY_NOT_BE_ANNOTATED" );

			return;
		}

		if ( constraintHelper.checkConstraint(
				mirror.getAnnotationType(), method.getReturnType()
		) != ConstraintCheckResult.ALLOWED ) {

			messager.reportError(
					method, mirror, "NOT_SUPPORTED_RETURN_TYPE",
					mirror.getAnnotationType().asElement().getSimpleName()
			);
		}
	}

	private void checkConstraintAtAnnotationType(TypeElement annotationType, AnnotationMirror annotationMirror) {

		if ( !constraintHelper.isConstraintAnnotation( annotationType ) ) {
			messager.reportError( annotationType, annotationMirror, "ONLY_CONSTRAINT_ANNOTATIONS_MAY_BE_ANNOTATED" );
		}

	}

	private void checkGraphValidationAnnotationAtField(
			VariableElement annotatedField, AnnotationMirror annotationMirror) {

		if ( isStaticElement( annotatedField ) ) {

			messager.reportError(
					annotatedField, annotationMirror,
					"STATIC_FIELDS_MAY_NOT_BE_ANNOTATED"
			);

			return;
		}

		if ( isPrimitiveType( annotatedField.asType() ) ) {

			messager.reportError(
					annotatedField, annotationMirror,
					"ATVALID_NOT_ALLOWED_AT_PRIMITIVE_FIELD"
			);
		}
	}

	private void checkGraphValidationAnnotationAtMethod(
			ExecutableElement method, AnnotationMirror annotationMirror) {

		if ( !isGetterMethod( method ) ) {

			messager.reportError(
					method, annotationMirror,
					"ONLY_GETTERS_MAY_BE_ANNOTATED"
			);

			return;
		}

		if ( isStaticElement( method ) ) {

			messager.reportError(
					method, annotationMirror,
					"STATIC_METHODS_MAY_NOT_BE_ANNOTATED"
			);

			return;
		}

		if ( isPrimitiveType( method.getReturnType() ) ) {

			messager.reportError(
					method, annotationMirror,
					"ATVALID_NOT_ALLOWED_AT_METHOD_RETURNING_PRIMITIVE_TYPE"
			);
		}
	}

	private boolean isGetterMethod(ExecutableElement method) {
		return isJavaBeanGetterName( method.getSimpleName().toString() )
				&& !hasParameters( method ) && hasReturnValue( method );
	}

	private boolean hasReturnValue(ExecutableElement method) {
		return method.getReturnType().getKind() != TypeKind.VOID;
	}

	private boolean hasParameters(ExecutableElement method) {
		return !method.getParameters().isEmpty();
	}

	private boolean isJavaBeanGetterName(String methodName) {
		return methodName.startsWith( "is" ) || methodName.startsWith( "has" ) || methodName.startsWith( "get" );
	}

	private boolean isStaticElement(Element element) {
		return element.getModifiers().contains( Modifier.STATIC );
	}

	private boolean isPrimitiveType(TypeMirror typeMirror) {
		return typeMirror.getKind().isPrimitive();
	}

}
