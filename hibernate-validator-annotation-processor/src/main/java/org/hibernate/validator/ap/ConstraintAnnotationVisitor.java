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
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementKindVisitor6;
import javax.tools.Diagnostic.Kind;

import org.hibernate.validator.ap.util.ConstraintHelper;

/**
 * An {@link ElementVisitor} that visits elements (type declarations, methods
 * and fields) annotated with constraint annotations from the Bean Validation
 * API.
 * 
 * TODO GM: visit type declarations.
 * 
 * @author Gunnar Morling.
 * 
 */
final class ConstraintAnnotationVisitor extends
		ElementKindVisitor6<Void, List<AnnotationMirror>> {
	
	private ProcessingEnvironment processingEnvironment;
	
	private ResourceBundle errorMessages;

	//TODO GM: make configurable using Options API
	private Kind messagingKind = Kind.ERROR;

	public ConstraintAnnotationVisitor(ProcessingEnvironment processingEnvironment) {
		
		this.processingEnvironment = processingEnvironment;
		errorMessages = ResourceBundle.getBundle("org.hibernate.validator.ap.ValidationProcessorMessages");
	}
	
	/**
	 * <p>
	 * Checks whether the given mirrors representing one or more constraint annotations are correctly
	 * specified at the given method. The following checks are performed:</p>
	 * <ul>
	 * <li>
	 * The method must be a JavaBeans getter method.
	 * </li> 
	 * <li>
	 * TODO GM:
	 * The return type of the method must be supported by the constraints.
	 * </li>
	 * </ul>
	 */
	@Override
	public Void visitExecutableAsMethod(ExecutableElement element,
			List<AnnotationMirror> mirrors) {
		
		for (AnnotationMirror oneAnnotationMirror : mirrors) {
			if(!isJavaBeanGetterName(element.getSimpleName().toString())) {
				
				processingEnvironment.getMessager().printMessage(
					messagingKind,
					errorMessages.getString("ONLY_GETTERS_MAY_BE_ANNOTATED"), element, oneAnnotationMirror);
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
		
		for (AnnotationMirror oneAnnotationMirror : mirrors) {
			
			ConstraintHelper builtInConstraintHelper = 
				new ConstraintHelper(processingEnvironment.getElementUtils(), processingEnvironment.getTypeUtils());
			
			boolean annotationAllowedAtElementType = 
				builtInConstraintHelper.isAnnotationAllowedAtElement(oneAnnotationMirror.getAnnotationType(), annotatedField);
			
			if(!annotationAllowedAtElementType) {
				processingEnvironment.getMessager().printMessage(
					messagingKind,
					MessageFormat.format(errorMessages.getString("NOT_SUPPORTED_TYPE"), oneAnnotationMirror.getAnnotationType().asElement().getSimpleName()), annotatedField, oneAnnotationMirror);
			}
		}
		
		return null;
	}
	
	private boolean isJavaBeanGetterName(String name) {
		return name.startsWith("is") || name.startsWith("has") || name.startsWith("get");
	}
	
}