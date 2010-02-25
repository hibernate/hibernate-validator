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
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeElement;

import org.hibernate.validator.ap.util.AnnotationApiHelper;

/**
 * An annotation processor for checking <a
 * href="http://jcp.org/en/jsr/detail?id=303">Bean Validation</a> constraints.
 * The processor supports the following options:
 * <ul>
 * <li><code>diagnosticKind</code>: the severity with which any occured problems
 * shall be reported. Must be given in form of the string representation of a
 * value from {@link javax.tools.Diagnostic.Kind}, e.g.
 * "diagnosticKind=WARNING". Default is Kind.ERROR.</li>
 * <li>TODO GM: validationMode: whether spec compliance shall be checked
 * strictly or loosely (e.g. by allowing validators for parametrized types)</li>
 * </ul>
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
// TODO GM: check @Valid annotation
// TODO GM: add documentation for AP to HV reference guide
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions(ConstraintAnnotationVisitor.DIAGNOSTIC_KIND_PROCESSOR_OPTION_NAME)
public class ConstraintValidationProcessor extends AbstractProcessor {

	/**
	 * Whether this processor claims all processed annotations exclusively or not.
	 */
	private static final boolean ANNOTATIONS_CLAIMED_EXCLUSIVELY = false;

	@Override
	public boolean process(
			final Set<? extends TypeElement> annotations,
			final RoundEnvironment roundEnvironment) {

		AnnotationApiHelper typeHelper = new AnnotationApiHelper(
				processingEnv.getElementUtils(), processingEnv.getTypeUtils()
		);

		ElementVisitor<Void, List<AnnotationMirror>> visitor = new ConstraintAnnotationVisitor( processingEnv );

		for ( TypeElement oneAnnotation : annotations ) {

			Set<? extends Element> elementsWithConstraintAnnotation =
					roundEnvironment.getElementsAnnotatedWith( oneAnnotation );

			for ( Element oneAnnotatedElement : elementsWithConstraintAnnotation ) {

				List<AnnotationMirror> mirrorsOfCurrentAnnotation =
						typeHelper.filterByType( oneAnnotatedElement.getAnnotationMirrors(), oneAnnotation.asType() );

				oneAnnotatedElement.accept( visitor, mirrorsOfCurrentAnnotation );
			}
		}

		return ANNOTATIONS_CLAIMED_EXCLUSIVELY;
	}

}
