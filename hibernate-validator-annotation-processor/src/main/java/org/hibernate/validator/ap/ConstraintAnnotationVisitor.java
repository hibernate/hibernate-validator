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

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementKindVisitor6;
import javax.tools.Diagnostic.Kind;

import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.ConstraintHelper;
import org.hibernate.validator.ap.util.ConstraintHelper.ConstraintCheckResult;

/**
 * An {@link ElementVisitor} that visits elements (type declarations, methods
 * and fields) annotated with constraint annotations from the Bean Validation
 * API.
 *
 * @author Gunnar Morling.
 */
final class ConstraintAnnotationVisitor extends ElementKindVisitor6<Void, List<AnnotationMirror>> {

	/**
	 * The name of the processor option for setting the diagnostic kind to be
	 * used when reporting errors during annotation processing. Can be set on
	 * the command line using the -A option, e.g.
	 * <code>-AdiagnosticKind=ERROR</code>.
	 */
	public final static String DIAGNOSTIC_KIND_PROCESSOR_OPTION_NAME = "diagnosticKind";

	/**
	 * The diagnostic kind to be used if no or an invalid kind is given as processor option.
	 */
	public final static Kind DEFAULT_DIAGNOSTIC_KIND = Kind.ERROR;

	/**
	 * The kind of diagnostic to be used when reporting any problems.
	 */
	private Kind diagnosticKind;

	private final ProcessingEnvironment processingEnvironment;

	private final ResourceBundle errorMessages;

	private final ConstraintHelper constraintHelper;

	private final AnnotationApiHelper annotationApiHelper;

	public ConstraintAnnotationVisitor(ProcessingEnvironment processingEnvironment) {

		this.processingEnvironment = processingEnvironment;

		errorMessages = ResourceBundle.getBundle( "org.hibernate.validator.ap.ValidationProcessorMessages" );

		annotationApiHelper = new AnnotationApiHelper(
				processingEnvironment.getElementUtils(), processingEnvironment.getTypeUtils()
		);

		constraintHelper = new ConstraintHelper(
				processingEnvironment.getElementUtils(), processingEnvironment.getTypeUtils(), annotationApiHelper
		);

		initializeDiagnosticKind();
	}

	private void initializeDiagnosticKind() {

		String diagnosticKindFromOptions = processingEnvironment.getOptions()
				.get( DIAGNOSTIC_KIND_PROCESSOR_OPTION_NAME );

		if ( diagnosticKindFromOptions != null ) {
			try {
				diagnosticKind = Kind.valueOf( diagnosticKindFromOptions );
			}
			catch ( IllegalArgumentException e ) {

				processingEnvironment.getMessager().printMessage(
						Kind.ERROR, MessageFormat.format(
								errorMessages.getString( "INVALID_DIAGNOSTIC_KIND_GIVEN" ), diagnosticKindFromOptions
						)
				);

				diagnosticKind = DEFAULT_DIAGNOSTIC_KIND;
			}
		}
		else {
			diagnosticKind = DEFAULT_DIAGNOSTIC_KIND;
		}
	}

	/**
	 * <p>
	 * Checks whether the given mirrors representing one or more constraint annotations are correctly
	 * specified at the given method. The following checks are performed:</p>
	 * <ul>
	 * <li>
	 * The method must be a JavaBeans getter method (name starts with "is", "get" or "has", no parameters)
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

			if ( constraintHelper.isConstraintAnnotation( oneAnnotationMirror ) ) {

				checkConstraintAtMethod( method, oneAnnotationMirror );
			}
			else if ( constraintHelper.isMultiValuedConstraint( oneAnnotationMirror ) ) {
				for ( AnnotationMirror onePartOfMultiValuedConstraint : constraintHelper.getPartsOfMultiValuedConstraint(
						oneAnnotationMirror
				) ) {
					checkConstraintAtMethod( method, onePartOfMultiValuedConstraint );
				}
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

			if ( constraintHelper.isConstraintAnnotation( oneAnnotationMirror ) ) {

				checkConstraintAtField( annotatedField, oneAnnotationMirror );
			}
			else if ( constraintHelper.isMultiValuedConstraint( oneAnnotationMirror ) ) {
				for ( AnnotationMirror onePartOfMultiValuedConstraint : constraintHelper.getPartsOfMultiValuedConstraint(
						oneAnnotationMirror
				) ) {
					checkConstraintAtField( annotatedField, onePartOfMultiValuedConstraint );
				}
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
	// TODO GM: do a more complete check of constraint annotation type
	// declarations:
	// 
	// - check, existence of groups(), message(), payload()
	// - check, retention policy
	// - check, that the set of supported types is not empty
	// - optionally check, that validated types resolve to non-parametrized types
	@Override
	public Void visitTypeAsAnnotationType(TypeElement annotationType,
										  List<AnnotationMirror> mirrors) {

		for ( AnnotationMirror oneAnnotationMirror : mirrors ) {

			if ( constraintHelper.isConstraintAnnotation( oneAnnotationMirror ) ) {

				checkConstraintAtAnnotationType( annotationType, oneAnnotationMirror );

			}
			else if ( constraintHelper.isMultiValuedConstraint( oneAnnotationMirror ) ) {

				for ( AnnotationMirror onePartOfMultiValuedConstraint : constraintHelper.getPartsOfMultiValuedConstraint(
						oneAnnotationMirror
				) ) {
					checkConstraintAtAnnotationType( annotationType, onePartOfMultiValuedConstraint );
				}
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

			if ( constraintHelper.isConstraintAnnotation( oneAnnotationMirror ) ) {

				checkConstraintAtType( annotatedType, oneAnnotationMirror );
			}
			else if ( constraintHelper.isMultiValuedConstraint( oneAnnotationMirror ) ) {
				for ( AnnotationMirror onePartOfMultiValuedConstraint : constraintHelper.getPartsOfMultiValuedConstraint(
						oneAnnotationMirror
				) ) {
					checkConstraintAtType( annotatedType, onePartOfMultiValuedConstraint );
				}
			}
		}

		return null;
	}

	private void checkConstraintAtType(TypeElement annotatedType, AnnotationMirror mirror) {

		if ( constraintHelper.checkConstraint(
				mirror.getAnnotationType(), annotatedType.asType()
		) != ConstraintCheckResult.ALLOWED ) {

			reportError(
					annotatedType, mirror, "NOT_SUPPORTED_TYPE",
					mirror.getAnnotationType().asElement().getSimpleName()
			);
		}
	}

	private void checkConstraintAtField(VariableElement annotatedField, AnnotationMirror annotationMirror) {

		if ( isStaticElement( annotatedField ) ) {

			reportError( annotatedField, annotationMirror, "STATIC_FIELDS_MAY_NOT_BE_ANNOTATED" );

			return;
		}

		if ( constraintHelper.checkConstraint(
				annotationMirror.getAnnotationType(), annotatedField.asType()
		) != ConstraintCheckResult.ALLOWED ) {

			reportError(
					annotatedField, annotationMirror, "NOT_SUPPORTED_TYPE",
					annotationMirror.getAnnotationType().asElement().getSimpleName()
			);
		}
	}

	private void checkConstraintAtMethod(ExecutableElement method, AnnotationMirror mirror) {

		if ( !isJavaBeanGetterName( method.getSimpleName().toString() ) ||
				hasParameters( method ) ) {

			reportError( method, mirror, "ONLY_GETTERS_MAY_BE_ANNOTATED" );

			return;
		}

		if ( isStaticElement( method ) ) {

			reportError( method, mirror, "STATIC_METHODS_MAY_NOT_BE_ANNOTATED" );

			return;
		}

		if ( constraintHelper.checkConstraint(
				mirror.getAnnotationType(), method.getReturnType()
		) != ConstraintCheckResult.ALLOWED ) {

			reportError(
					method, mirror, "NOT_SUPPORTED_RETURN_TYPE",
					mirror.getAnnotationType().asElement().getSimpleName()
			);
		}
	}

	private void checkConstraintAtAnnotationType(TypeElement annotationType, AnnotationMirror annotationMirror) {

		if ( !constraintHelper.isConstraintAnnotation( annotationType ) ) {
			reportError( annotationType, annotationMirror, "ONLY_CONSTRAINT_ANNOTATIONS_MAY_BE_ANNOTATED" );
		}

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

	/**
	 * Reports an error at the given location using the given message key and
	 * optionally the given message parameters.
	 *
	 * @param element The element at which the error shall be reported.
	 * @param annotationMirror The annotation mirror at which the error shall be reported.
	 * @param messageKey The message key to be used to retrieve the text.
	 * @param messageParameters An optional array of message parameters to be put into the
	 * message using a {@link MessageFormat}.
	 */
	private void reportError(Element element, AnnotationMirror annotationMirror, String messageKey, Object... messageParameters) {

		String message;

		if ( messageParameters == null ) {
			message = errorMessages.getString( messageKey );
		}
		else {
			message = MessageFormat.format( errorMessages.getString( messageKey ), messageParameters );
		}

		processingEnvironment.getMessager().printMessage(
				diagnosticKind, message, element, annotationMirror
		);
	}

}