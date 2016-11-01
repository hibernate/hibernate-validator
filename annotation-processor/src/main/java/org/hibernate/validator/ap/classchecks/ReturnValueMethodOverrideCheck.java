/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.classchecks;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.util.CollectionHelper;
import org.hibernate.validator.ap.util.ConstraintHelper;

/**
 * Checks if the return value of overridden and overriding methods have correct set of annotations.
 * Return value constraints of must not be weakened in subtypes. One must not mark a method return value
 * for cascaded validation more than once in a line of a class hierarchy. In other words, overriding methods
 * on sub types (be it sub classes/interfaces or interface implementations) cannot mark the return value
 * for cascaded validation if the return value has already been marked on the overridden method of the super
 * type or interface.
 *
 * @author Marko Bekhta
 */
public class ReturnValueMethodOverrideCheck extends AbstractMethodOverrideCheck {

	public ReturnValueMethodOverrideCheck(Elements elementUtils, Types typeUtils, ConstraintHelper constraintHelper) {
		super( elementUtils, typeUtils, constraintHelper );
	}

	@Override
	protected Collection<ConstraintCheckIssue> checkMethodInternal(ExecutableElement currentMethod, Map<Boolean, List<ExecutableElement>> overriddenMethods) {
		// if this method gets executed it means that current method has a @Valid annotation and we
		// need to check if there's no more @Valid annotations in the hierarchy for this method
		Collection<ConstraintCheckIssue> issues = CollectionHelper.newArrayList();
		for ( List<ExecutableElement> methods : overriddenMethods.values() ) {
			for ( ExecutableElement overriddenMethod : methods ) {
				if ( methodIsAnnotatedWithValid( overriddenMethod ) ) {
					issues.add( ConstraintCheckIssue.error(
							currentMethod, null, "INCORRECT_METHOD_RETURN_OVERRIDING", currentMethod.getSimpleName().toString(), getEnclosingTypeElementQualifiedName( currentMethod ), getEnclosingTypeElementQualifiedName( overriddenMethod )
					) );
				}
			}
		}

		return issues;
	}

	@Override
	protected boolean needToPerformAnyChecks(ExecutableElement currentMethod) {
		// if the method returns void, there's no need to check it
		// and if method contains @Valid annotation we need to check it
		return !currentMethod.getReturnType().getKind().equals( TypeKind.VOID ) && methodIsAnnotatedWithValid( currentMethod );
	}

	/**
	 * Check if there's a {@code @Valid} annotation present on return value of a given method.
	 *
	 * @param method a method to check for annotation presence
	 *
	 * @return {@code true} if {@code @Valid} annotation is present on return value of a given method, {@code false} otherwise
	 */
	private boolean methodIsAnnotatedWithValid(ExecutableElement method) {
		for ( AnnotationMirror annotationMirror : method.getAnnotationMirrors() ) {
			if ( ConstraintHelper.AnnotationType.GRAPH_VALIDATION_ANNOTATION.equals( constraintHelper.getAnnotationType( annotationMirror ) ) ) {
				return true;
			}
		}
		return false;
	}
}
