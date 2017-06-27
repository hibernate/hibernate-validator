/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.checks;

import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import org.hibernate.validator.ap.internal.util.CollectionHelper;

/**
 * Checks, that the given element is not a static element. Applies to fields
 * and methods.
 *
 * @author Gunnar Morling
 */
public class StaticCheck extends AbstractConstraintCheck {

	@Override
	public Set<ConstraintCheckIssue> checkField(VariableElement element, AnnotationMirror annotation) {

		return checkInternal( element, annotation, "STATIC_FIELDS_MAY_NOT_BE_ANNOTATED" );
	}

	@Override
	public Set<ConstraintCheckIssue> checkMethod(ExecutableElement element, AnnotationMirror annotation) {

		return checkInternal( element, annotation, "STATIC_METHODS_MAY_NOT_BE_ANNOTATED" );
	}

	private Set<ConstraintCheckIssue> checkInternal(Element element,
													AnnotationMirror annotation, String messageKey) {
		if ( isStaticElement( element ) ) {

			return CollectionHelper.asSet( ConstraintCheckIssue.error( element, annotation, messageKey ) );
		}

		return Collections.emptySet();
	}

	private boolean isStaticElement(Element element) {
		return element.getModifiers().contains( Modifier.STATIC );
	}

}
