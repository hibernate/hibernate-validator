// $Id: JPAMetaModelEntityProcessor.java 17946 2009-11-06 18:23:48Z hardy.ferentschik $
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


import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import static javax.lang.model.SourceVersion.RELEASE_6;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import javax.tools.Diagnostic;


/**
 * Annotation processor for validating Bean Validation constraints.
 *
 * @author Hardy Ferentschik
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(RELEASE_6)
public class ConstraintValidationProcessor extends AbstractProcessor {
	private static final Boolean ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS = Boolean.FALSE;
	private Messager messager;

	public void init(ProcessingEnvironment env) {
		super.init( env );
		messager = env.getMessager();
		messager.printMessage( Diagnostic.Kind.NOTE, "Init Processor " + this );
	}

	@Override
	public boolean process(final Set<? extends TypeElement> annotations,
						   final RoundEnvironment roundEnvironment) {

		if ( roundEnvironment.processingOver() ) {
			messager.printMessage( Diagnostic.Kind.NOTE, "Last processing round." );
			return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
		}

		Set<? extends Element> elements = roundEnvironment.getRootElements();
		for ( Element element : elements ) {
			messager.printMessage( Diagnostic.Kind.NOTE, "Processing " + element.toString() );
		}

		return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
	}
}
