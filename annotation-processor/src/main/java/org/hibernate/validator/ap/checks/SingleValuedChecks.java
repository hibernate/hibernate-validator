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
package org.hibernate.validator.ap.checks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.hibernate.validator.ap.util.CollectionHelper;

/**
 * A {@link ConstraintChecks} implementation that simply executes all
 * contained checks against given elements and annotations.
 *
 * @author Gunnar Morling
 */
public class SingleValuedChecks implements ConstraintChecks {

	//TODO GM: the "ordered set" character is currently ensured by adding
	//each check only once in ConstraintCheckFactory. Should this be a real set?
	private final List<ConstraintCheck> checks;

	/**
	 * Creates a new SingleValuedChecks.
	 *
	 * @param checks The checks to execute.
	 */
	public SingleValuedChecks(ConstraintCheck... checks) {

		if ( checks == null ) {
			this.checks = Collections.emptyList();
		}
		else {
			this.checks = Arrays.asList( checks );
		}
	}

	public Set<ConstraintCheckError> execute(Element element, AnnotationMirror annotation) {

		Set<ConstraintCheckError> theValue = CollectionHelper.newHashSet();

		//for each check execute the check method appropriate for the kind of
		//the given element
		for ( ConstraintCheck oneCheck : checks ) {

			if ( element.getKind() == ElementKind.FIELD ) {
				theValue.addAll( oneCheck.checkField( ( VariableElement ) element, annotation ) );
			}
			else if ( element.getKind() == ElementKind.METHOD ) {
				theValue.addAll( oneCheck.checkMethod( ( ExecutableElement ) element, annotation ) );
			}
			else if ( element.getKind() == ElementKind.ANNOTATION_TYPE ) {
				theValue.addAll( oneCheck.checkAnnotationType( ( TypeElement ) element, annotation ) );
			}
			else if (
					element.getKind() == ElementKind.CLASS ||
							element.getKind() == ElementKind.INTERFACE ||
							element.getKind() == ElementKind.ENUM ) {

				theValue.addAll( oneCheck.checkNonAnnotationType( ( TypeElement ) element, annotation ) );
			}

			if ( !theValue.isEmpty() ) {
				return theValue;
			}
		}

		return theValue;
	}
}
