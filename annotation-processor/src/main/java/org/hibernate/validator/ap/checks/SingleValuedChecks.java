/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
				theValue.addAll( oneCheck.checkField( (VariableElement) element, annotation ) );
			}
			else if ( element.getKind() == ElementKind.METHOD ) {
				theValue.addAll( oneCheck.checkMethod( (ExecutableElement) element, annotation ) );
			}
			else if ( element.getKind() == ElementKind.ANNOTATION_TYPE ) {
				theValue.addAll( oneCheck.checkAnnotationType( (TypeElement) element, annotation ) );
			}
			else if (
					element.getKind() == ElementKind.CLASS ||
							element.getKind() == ElementKind.INTERFACE ||
							element.getKind() == ElementKind.ENUM ) {

				theValue.addAll( oneCheck.checkNonAnnotationType( (TypeElement) element, annotation ) );
			}

			if ( !theValue.isEmpty() ) {
				return theValue;
			}
		}

		return theValue;
	}
}
