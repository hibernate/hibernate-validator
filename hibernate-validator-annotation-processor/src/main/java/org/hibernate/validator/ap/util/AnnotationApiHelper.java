// $Id: ConstraintHelper.java 17946 2009-11-06 18:23:48Z hardy.ferentschik $
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
package org.hibernate.validator.ap.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * A helper class providing some useful methods to work with types
 * from the JSR-269-API.
 * 
 * @author Gunnar Morling
 *
 */
public class AnnotationApiHelper {

	private Elements elementUtils;
	
	private Types typeUtils;

	public AnnotationApiHelper(Elements elementUtils, Types typeUtils) {

		this.elementUtils = elementUtils;
		this.typeUtils = typeUtils;
	}
	
	/**
	 * Returns a list containing those annotation mirrors from the input list,
	 * which are of type <code>annotationType</code>. The input collection
	 * remains untouched.
	 * 
	 * @param annotationMirrors
	 *            A list of annotation mirrors.
	 * @param annotationType
	 *            The type to be compared against.
	 * 
	 * @return A list with those annotation mirrors from the input list, which
	 *         are of type <code>annotationType</code>. May be empty but never
	 *         null.
	 */
	public List<AnnotationMirror> filterByType(List<? extends AnnotationMirror> annotationMirrors, TypeMirror annotationType) {

		List<AnnotationMirror> theValue = new ArrayList<AnnotationMirror>();

		if(annotationMirrors == null || annotationType == null) {
			return theValue;
		}
		
		for (AnnotationMirror oneAnnotationMirror : annotationMirrors) {

			if (typeUtils.isSameType(oneAnnotationMirror.getAnnotationType(), annotationType)) {
				theValue.add(oneAnnotationMirror);
			}
		}

		return theValue;
	}
	
    public AnnotationMirror getMirror(List<? extends AnnotationMirror> annotationMirrors, Class<? extends Annotation> annotationClazz) {
    	
		if(annotationMirrors == null || annotationClazz == null) {
			return null;
		}
		
		for (AnnotationMirror oneAnnotationMirror : annotationMirrors) {

			TypeElement typeElement = elementUtils.getTypeElement(annotationClazz.getCanonicalName());
						
			if (typeUtils.isSameType(oneAnnotationMirror.getAnnotationType(), typeElement.asType())) {
				return oneAnnotationMirror;
			}
		}

        return null;
    }

}
