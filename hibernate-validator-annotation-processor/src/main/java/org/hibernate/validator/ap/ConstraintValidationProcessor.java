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
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.MessagerAdapter;

/**
 * An annotation processor for checking <a
 * href="http://jcp.org/en/jsr/detail?id=303">Bean Validation</a> constraints.
 * The processor supports the following options:
 * <ul>
 * <li><code>diagnosticKind</code>: the severity with which any occurred problems
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
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions(ConstraintValidationProcessor.DIAGNOSTIC_KIND_PROCESSOR_OPTION_NAME)
public class ConstraintValidationProcessor extends AbstractProcessor {

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
	 * Whether this processor claims all processed annotations exclusively or not.
	 */
	private static final boolean ANNOTATIONS_CLAIMED_EXCLUSIVELY = false;

	/**
	 * The messager to be used for error reports.
	 */
	private MessagerAdapter messager;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {

		super.init( processingEnv );

		messager = new MessagerAdapter( processingEnv.getMessager(), getDiagnosticKind() );
	}

	@Override
	public boolean process(
			final Set<? extends TypeElement> annotations,
			final RoundEnvironment roundEnvironment) {

		AnnotationApiHelper typeHelper = new AnnotationApiHelper(
				processingEnv.getElementUtils(), processingEnv.getTypeUtils()
		);

		ElementVisitor<Void, List<AnnotationMirror>> visitor = new ConstraintAnnotationVisitor(
				processingEnv, messager
		);

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

	/**
	 * Retrieves the diagnostic kind to be used for error messages. If given in processor options, it
	 * will be taken from there, otherwise the default value Kind.ERROR will be returned.
	 *
	 * @return The diagnostic kind to be used for error messages.
	 */
	private Kind getDiagnosticKind() {

		String diagnosticKindFromOptions = processingEnv.getOptions()
				.get( DIAGNOSTIC_KIND_PROCESSOR_OPTION_NAME );

		if ( diagnosticKindFromOptions != null ) {
			try {
				return Kind.valueOf( diagnosticKindFromOptions );
			}
			catch ( IllegalArgumentException e ) {
				super.processingEnv.getMessager().printMessage(
						Kind.WARNING, MessageFormat.format(
								"The given value {0} is no valid diagnostic kind. {1} will be used.",
								diagnosticKindFromOptions,
								DEFAULT_DIAGNOSTIC_KIND
						)
				);
			}
		}

		return DEFAULT_DIAGNOSTIC_KIND;
	}

}
