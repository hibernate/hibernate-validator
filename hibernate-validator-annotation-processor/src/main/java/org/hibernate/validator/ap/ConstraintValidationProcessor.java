// $Id: ConstraintValidationProcessor.java 17946 2009-11-06 18:23:48Z hardy.ferentschik $
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
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.ConstraintHelper;


/**
 * Annotation processor for validating Bean Validation constraints.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ConstraintValidationProcessor extends AbstractProcessor {
	
	/**
	 * Whether this processor claims all processed annotations exclusively or not.
	 */
	private static final boolean ANNOTATIONS_CLAIMED_EXCLUSIVELY = false;
	
	
	@Override
	public boolean process(
		final Set<? extends TypeElement> annotations,
		final RoundEnvironment roundEnvironment) {
		
		AnnotationApiHelper typeHelper = new AnnotationApiHelper(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
		ConstraintAnnotationVisitor v = new ConstraintAnnotationVisitor(processingEnv);
		ConstraintHelper constraintHelper = new ConstraintHelper(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
		
		for (TypeElement oneAnnotation : annotations) {
			
			//only constraint annotations are relevant
			if(!constraintHelper.isConstraintAnnotation(oneAnnotation)) {
				continue;
			}
			
			Set<? extends Element> elementsWithConstraintAnnotation = 
				roundEnvironment.getElementsAnnotatedWith(oneAnnotation);
			
			for (Element oneAnnotatedElement : elementsWithConstraintAnnotation) {
				
				List<AnnotationMirror> mirrorsOfCurrentAnnotation = 
					typeHelper.filterByType(oneAnnotatedElement.getAnnotationMirrors(), oneAnnotation.asType());
				
				
				oneAnnotatedElement.accept(v, mirrorsOfCurrentAnnotation);			
			}
		}	
		
		return ANNOTATIONS_CLAIMED_EXCLUSIVELY;
	}
	
}
