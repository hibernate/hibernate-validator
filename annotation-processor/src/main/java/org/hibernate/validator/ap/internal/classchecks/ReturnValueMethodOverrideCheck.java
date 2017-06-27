/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.classchecks;

import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.internal.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.internal.util.CollectionHelper;
import org.hibernate.validator.ap.internal.util.ConstraintHelper;

/**
 * Checks if the return value of overridden and overriding methods respects the inheritance rules.
 * <p>
 * Return value constraints of a method must not be weakened in subtypes. One must not mark a method return value
 * for cascaded validation more than once in a line of a class hierarchy. In other words, overriding methods
 * on subtypes (be it sub classes/interfaces or interface implementations) cannot mark the return value
 * for cascaded validation if the return value has already been marked on the overridden method of the super
 * type or interface.
 *
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public class ReturnValueMethodOverrideCheck extends AbstractMethodOverrideCheck {

	public ReturnValueMethodOverrideCheck(Elements elementUtils, Types typeUtils, ConstraintHelper constraintHelper) {
		super( elementUtils, typeUtils, constraintHelper );
	}

	@Override
	protected Set<ConstraintCheckIssue> checkMethodInternal(ExecutableElement currentMethod, MethodInheritanceTree methodInheritanceTree) {
		// if this method gets executed it means that the current method has a @Valid annotation and we
		// need to check that there is no other @Valid annotations in the hierarchy of this method
		Set<ConstraintCheckIssue> issues = CollectionHelper.newHashSet();
		for ( ExecutableElement overriddenMethod : methodInheritanceTree.getOverriddenMethods() ) {
			if ( methodIsAnnotatedWithValid( overriddenMethod ) ) {
				issues.add( ConstraintCheckIssue.error(
						currentMethod,
						null,
						"INCORRECT_METHOD_RETURN_VALUE_OVERRIDING",
						getEnclosingTypeElementQualifiedName( overriddenMethod )
				) );
			}
		}

		return issues;
	}

	@Override
	protected boolean needToPerformAnyChecks(ExecutableElement currentMethod) {
		// we only check the method if:
		// - it does not return void
		// - it is marked with @Valid
		return !currentMethod.getReturnType().getKind().equals( TypeKind.VOID ) && methodIsAnnotatedWithValid( currentMethod );
	}

	/**
	 * Check if there is a {@code @Valid} annotation present on the method.
	 *
	 * @param method a method to check for annotation presence
	 * @return {@code true} if {@code @Valid} annotation is present on return value of a given method, {@code false}
	 * otherwise
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
