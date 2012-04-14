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
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementKindVisitor6;
import javax.tools.Diagnostic.Kind;

import org.hibernate.validator.ap.checks.ConstraintCheckError;
import org.hibernate.validator.ap.checks.ConstraintCheckFactory;
import org.hibernate.validator.ap.checks.ConstraintChecks;
import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.Configuration;
import org.hibernate.validator.ap.util.ConstraintHelper;
import org.hibernate.validator.ap.util.MessagerAdapter;

/**
 * An {@link javax.lang.model.element.ElementVisitor} that visits annotated elements (type declarations,
 * methods and fields) and applies different {@link org.hibernate.validator.ap.checks.ConstraintCheck}s to them.
 * Each {@link ConstraintCheckError} occurred will be reported using the
 * {@link javax.annotation.processing.Messager} API.
 *
 * @author Gunnar Morling
 */
final class ConstraintAnnotationVisitor extends ElementKindVisitor6<Void, List<AnnotationMirror>> {

	private final MessagerAdapter messager;

	private final ConstraintCheckFactory constraintCheckFactory;

	private final boolean verbose;

	public ConstraintAnnotationVisitor(
			ProcessingEnvironment processingEnvironment, MessagerAdapter messager, Configuration configuration) {

		this.messager = messager;
		this.verbose = configuration.isVerbose();

		AnnotationApiHelper annotationApiHelper = new AnnotationApiHelper(
				processingEnvironment.getElementUtils(), processingEnvironment.getTypeUtils()
		);

		ConstraintHelper constraintHelper = new ConstraintHelper(
				processingEnvironment.getTypeUtils(), annotationApiHelper
		);

		constraintCheckFactory = new ConstraintCheckFactory(
				processingEnvironment.getTypeUtils(),
				constraintHelper,
				annotationApiHelper,
				configuration.methodConstraintsSupported()
		);
	}

	/**
	 * <p>
	 * Checks whether the given annotations are correctly specified at the given
	 * method. The following checks are performed:
	 * </p>
	 * <ul>
	 * <li>
	 * Constraint annotations may only be given at non-static, JavaBeans getter
	 * methods which's return type is supported by the constraints.</li>
	 * <li>
	 * The <code>@Valid</code> annotation may only be given at non-static,
	 * non-primitive JavaBeans getter methods.</li>
	 * </ul>
	 */
	@Override
	public Void visitExecutableAsMethod(ExecutableElement method,
										List<AnnotationMirror> mirrors) {

		checkConstraints( method, mirrors );

		return null;
	}

	/**
	 * <p>
	 * Checks whether the given annotations are correctly specified at the given
	 * field. The following checks are performed:
	 * </p>
	 * <ul>
	 * <li>
	 * Constraint annotations may only be given at non-static fields which's
	 * type is supported by the constraints.</li>
	 * <li>
	 * The <code>@Valid</code> annotation may only be given at non-static,
	 * non-primitive fields.</li>
	 * </ul>
	 */
	@Override
	public Void visitVariableAsField(VariableElement annotatedField, List<AnnotationMirror> mirrors) {

		checkConstraints( annotatedField, mirrors );

		return null;
	}

	/**
	 * <p>
	 * Checks whether the given annotations are correctly specified at the given
	 * annotation type declaration. The following checks are performed:
	 * </p>
	 * <ul>
	 * <li>
	 * The only annotation types allowed to be annotated with other constraint
	 * annotations are composed constraint annotation type declarations.</li>
	 * </ul>
	 */
	@Override
	public Void visitTypeAsAnnotationType(TypeElement annotationType,
										  List<AnnotationMirror> mirrors) {

		checkConstraints( annotationType, mirrors );

		return null;
	}

	/**
	 * <p>
	 * Checks whether the given annotations are correctly specified at the given
	 * class type declaration. The following checks are performed:
	 * </p>
	 * <ul>
	 * <li>
	 * Constraint annotations may at types supported by the constraints.</li>
	 * <li>
	 * </ul>
	 */
	@Override
	public Void visitTypeAsClass(TypeElement e, List<AnnotationMirror> p) {

		checkConstraints( e, p );
		return null;
	}

	/**
	 * <p>
	 * Checks whether the given annotations are correctly specified at the given
	 * enum type declaration. The following checks are performed:
	 * </p>
	 * <ul>
	 * <li>
	 * Constraint annotations may at types supported by the constraints.</li>
	 * <li>
	 * </ul>
	 */
	@Override
	public Void visitTypeAsEnum(TypeElement e, List<AnnotationMirror> p) {

		checkConstraints( e, p );
		return null;
	}

	/**
	 * <p>
	 * Checks whether the given annotations are correctly specified at the given
	 * interface type declaration. The following checks are performed:
	 * </p>
	 * <ul>
	 * <li>
	 * Constraint annotations may at types supported by the constraints.</li>
	 * <li>
	 * </ul>
	 */
	@Override
	public Void visitTypeAsInterface(TypeElement e, List<AnnotationMirror> p) {

		checkConstraints( e, p );
		return null;
	}

	/**
	 * Retrieves the checks required for the given element and annotations,
	 * executes them and reports all occurred errors.
	 *
	 * @param annotatedElement The element to check.
	 * @param mirrors The annotations to check.
	 */
	private void checkConstraints(Element annotatedElement, List<AnnotationMirror> mirrors) {

		for ( AnnotationMirror oneAnnotationMirror : mirrors ) {

			try {

				ConstraintChecks constraintChecks = constraintCheckFactory.getConstraintChecks(
						annotatedElement, oneAnnotationMirror
				);
				Set<ConstraintCheckError> errors = constraintChecks.execute( annotatedElement, oneAnnotationMirror );
				messager.reportErrors( errors );
			}
			//HV-293: if single constraints can't be properly checked, report this and
			//proceed with next constraints
			catch ( Exception e ) {

				if ( verbose ) {
					messager.getDelegate()
							.printMessage( Kind.NOTE, e.getMessage() != null ? e.getMessage() : e.toString(), annotatedElement, oneAnnotationMirror );
				}
			}
		}
	}

}
