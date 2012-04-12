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
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeElement;

import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.Configuration;
import org.hibernate.validator.ap.util.MessagerAdapter;

/**
 * An annotation processor for checking <a
 * href="http://jcp.org/en/jsr/detail?id=303">Bean Validation</a> constraints.
 * The processor supports the following options:
 * <ul>
 * <li><code>diagnosticKind</code>: the severity with which any occurred
 * problems shall be reported. Must be given in form of the string
 * representation of a value from {@link javax.tools.Diagnostic.Kind}, e.g.
 * "diagnosticKind=WARNING". Default is Kind.ERROR.</li>
 * <li><code>verbose</code>: whether a verbose output shall be created or not.
 * Must be given as String parsable by {@link Boolean#parseBoolean}. Default is
 * <code>false</code>.</li>
 * <li><code>methodConstraintsSupported</code>: Whether constraints at other
 * methods than JavaBeans getter methods may be annotated with constraints or
 * not. Must be given as String parsable by {@link Boolean#parseBoolean}. Can be
 * set to <code>false</code> in order to allow only getter based property
 * constraints but not method level constraints as supported by Hibernate
 * Validator. Default is <code>true</code>.</li>
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
@SupportedAnnotationTypes("*")
@SupportedOptions({
		Configuration.DIAGNOSTIC_KIND_PROCESSOR_OPTION,
		Configuration.VERBOSE_PROCESSOR_OPTION,
		Configuration.METHOD_CONSTRAINTS_SUPPORTED_PROCESSOR_OPTION
})
public class ConstraintValidationProcessor extends AbstractProcessor {

	/**
	 * Whether this processor claims all processed annotations exclusively or not.
	 */
	private static final boolean ANNOTATIONS_CLAIMED_EXCLUSIVELY = false;

	/**
	 * The messager to be used for error reports.
	 */
	private MessagerAdapter messager;

	/**
	 * Provides access to this processor's configuration options.
	 */
	private Configuration configuration;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {

		super.init( processingEnv );

		configuration = new Configuration( processingEnv.getOptions(), processingEnv.getMessager() );
		messager = new MessagerAdapter( processingEnv.getMessager(), configuration.getDiagnosticKind() );
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(
			final Set<? extends TypeElement> annotations,
			final RoundEnvironment roundEnvironment) {

		AnnotationApiHelper typeHelper = new AnnotationApiHelper(
				processingEnv.getElementUtils(), processingEnv.getTypeUtils()
		);

		ElementVisitor<Void, List<AnnotationMirror>> visitor = new ConstraintAnnotationVisitor(
				processingEnv, messager, configuration
		);

		for ( TypeElement oneAnnotation : annotations ) {

			//Indicates that the annotation's type isn't on the class path of the compiled
			//project. Let the compiler deal with that and print an appropriate error.
			if ( oneAnnotation.getKind() != ElementKind.ANNOTATION_TYPE ) {
				continue;
			}

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
