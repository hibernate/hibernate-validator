// $Id: ConstraintAnnotationVisitor.java 17946 2009-11-06 18:23:48Z hardy.ferentschik $
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
import javax.validation.Constraint;

import org.hibernate.validator.ap.util.ConstraintHelper;

/**
 * An {@link ElementVisitor} that visits elements (type declarations, methods
 * and fields) annotated with constraint annotations from the Bean Validation
 * API.
 *
 * @author Gunnar Morling.
 */
final class ConstraintAnnotationVisitor extends ElementKindVisitor6<Void, List<AnnotationMirror>> {
	private final ProcessingEnvironment processingEnvironment;

	private final ResourceBundle errorMessages;

	//TODO GM: make configurable using Options API
	private final Kind messagingKind = Kind.ERROR;

	private final ConstraintHelper constraintHelper;

	public ConstraintAnnotationVisitor(ProcessingEnvironment processingEnvironment) {

		this.processingEnvironment = processingEnvironment;

		errorMessages = ResourceBundle.getBundle( "org.hibernate.validator.ap.ValidationProcessorMessages" );
		constraintHelper = new ConstraintHelper(
				processingEnvironment.getElementUtils(), processingEnvironment.getTypeUtils()
		);
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
	public Void visitExecutableAsMethod(ExecutableElement element,
										List<AnnotationMirror> mirrors) {

		for ( AnnotationMirror oneAnnotationMirror : mirrors ) {

			if ( !isJavaBeanGetterName( element.getSimpleName().toString() ) ||
					hasParameters( element ) ) {

				reportError( element, oneAnnotationMirror, "ONLY_GETTERS_MAY_BE_ANNOTATED" );

				continue;
			}

			if ( isStaticMethod( element ) ) {

				reportError( element, oneAnnotationMirror, "STATIC_METHODS_MAY_NOT_BE_ANNOTATED" );

				continue;
			}

			if ( !constraintHelper.isAnnotationAllowedAtMethod( oneAnnotationMirror.getAnnotationType(), element ) ) {

				reportError(
						element, oneAnnotationMirror, "NOT_SUPPORTED_RETURN_TYPE",
						oneAnnotationMirror.getAnnotationType().asElement().getSimpleName()
				);
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
	 * </ul>
	 */
	@Override
	public Void visitVariableAsField(VariableElement annotatedField, List<AnnotationMirror> mirrors) {

		for ( AnnotationMirror oneAnnotationMirror : mirrors ) {

			if ( !constraintHelper.isAnnotationAllowedAtField(
					oneAnnotationMirror.getAnnotationType(), annotatedField
			) ) {

				reportError(
						annotatedField, oneAnnotationMirror, "NOT_SUPPORTED_TYPE",
						oneAnnotationMirror.getAnnotationType().asElement().getSimpleName()
				);
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
	public Void visitTypeAsAnnotationType(TypeElement e,
										  List<AnnotationMirror> mirrors) {

		if ( !isConstraintAnnotation( e ) ) {

			for ( AnnotationMirror oneAnnotationMirror : mirrors ) {
				reportError( e, oneAnnotationMirror, "ONLY_CONSTRAINT_ANNOTATIONS_MAY_BE_ANNOTATED" );
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

			if ( !constraintHelper.isAnnotationAllowedAtType(
					oneAnnotationMirror.getAnnotationType(), annotatedType
			) ) {

				reportError(
						annotatedType, oneAnnotationMirror, "NOT_SUPPORTED_TYPE",
						oneAnnotationMirror.getAnnotationType().asElement().getSimpleName()
				);
			}
		}

		return null;
	}

	private boolean hasParameters(ExecutableElement method) {
		return !method.getParameters().isEmpty();
	}

	private boolean isJavaBeanGetterName(String methodName) {
		return methodName.startsWith( "is" ) || methodName.startsWith( "has" ) || methodName.startsWith( "get" );
	}

	private boolean isStaticMethod(ExecutableElement method) {
		return method.getModifiers().contains( Modifier.STATIC );
	}

	private boolean isConstraintAnnotation(TypeElement e) {
		return e.getAnnotation( Constraint.class ) != null;
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
				messagingKind, message, element, annotationMirror
		);
	}

}