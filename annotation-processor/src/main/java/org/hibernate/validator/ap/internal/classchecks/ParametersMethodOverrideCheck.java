/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.classchecks;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.internal.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.internal.util.CollectionHelper;
import org.hibernate.validator.ap.internal.util.ConstraintHelper;
import org.hibernate.validator.ap.internal.util.StringHelper;

/**
 * Checks if the parameters of overridden and overriding methods have correctly placed annotations.
 * Parameter constraints must not be strengthened in subtypes. The two rules implemented in this check are:
 * <ul>
 *     <li>
 *         In subtypes (be it sub classes/interfaces or interface implementations), no parameter constraints may be declared on overridden or
 *         implemented methods, nor may parameters be marked for cascaded validation. This would pose a strengthening of preconditions to be fulfilled by the caller.
 *     </li>
 *     <li>
 *         If a subtype overrides/implements a method originally defined in several parallel types of the hierarchy (e.g. two interfaces not extending each other,
 *         or a class and an interface not implemented by said class), no parameter constraints may be declared for that method at all nor parameters be marked for
 *         cascaded validation. This again is to avoid an unexpected strengthening of preconditions to be fulfilled by the caller.
 *     </li>
 * </ul>
 *
 * @author Marko Bekhta
 */
public class ParametersMethodOverrideCheck extends AbstractMethodOverrideCheck {

	public ParametersMethodOverrideCheck(Elements elementUtils, Types typeUtils, ConstraintHelper constraintHelper) {
		super( elementUtils, typeUtils, constraintHelper );
	}

	@Override
	protected Set<ConstraintCheckIssue> checkMethodInternal(ExecutableElement currentMethod, MethodInheritanceTree methodInheritanceTree) {
		// if you have 2 parallel hierarchies both of which implementing the same method,
		// you can't define a parameter constraint at all for this method (anywhere in the hierarchy, not even once)
		if ( methodInheritanceTree.hasParallelDefinitions() ) {
			// it means we have more than one top level method and as a result there cannot be any annotations present in the hierarchy
			Set<ConstraintCheckIssue> issues = CollectionHelper.newHashSet();
			for ( ExecutableElement method : methodInheritanceTree.getAllMethods() ) {
				if ( hasAnnotationsOnParameters( method ) ) {
					issues.add( ConstraintCheckIssue.error(
							currentMethod,
							null,
							"INCORRECT_METHOD_PARAMETERS_PARALLEL_IMPLEMENTATION_OVERRIDING",
							getEnclosingTypeElementQualifiedNames( methodInheritanceTree.getTopLevelMethods() )
					) );
				}
			}
			if ( !issues.isEmpty() ) {
				return issues;
			}
		}

		// you can't define a constraint on a parameter of an overriding/implementing method or mark it for cascaded validation
		if ( hasAnnotationsOnParameters( currentMethod ) ) {
			Set<ExecutableElement> overriddenMethods = methodInheritanceTree.getOverriddenMethods();

			return CollectionHelper.asSet( ConstraintCheckIssue.error(
					currentMethod,
					null,
					"INCORRECT_METHOD_PARAMETERS_OVERRIDING",
					getEnclosingTypeElementQualifiedNames( overriddenMethods )
			) );
		}

		return Collections.emptySet();
	}

	@Override
	protected boolean needToPerformAnyChecks(ExecutableElement currentMethod) {
		// if the method doesn't have any parameters, there's no need to check it
		return !currentMethod.getParameters().isEmpty();
	}

	/**
	 * Checks if a given method has any constraint or cascaded validation annotations on its parameters.
	 *
	 * @param method the method to check
	 * @return {@code true} if a constraint or cascaded annotations are present on any of the method parameters,
	 * {@code false} otherwise
	 */
	private boolean hasAnnotationsOnParameters(ExecutableElement method) {
		for ( VariableElement parameter : method.getParameters() ) {
			for ( AnnotationMirror annotationMirror : parameter.getAnnotationMirrors() ) {
				ConstraintHelper.AnnotationType annotationType = constraintHelper.getAnnotationType( annotationMirror );
				if ( ConstraintHelper.AnnotationType.CONSTRAINT_ANNOTATION.equals( annotationType )
						|| ConstraintHelper.AnnotationType.MULTI_VALUED_CONSTRAINT_ANNOTATION.equals( annotationType )
						|| ConstraintHelper.AnnotationType.GRAPH_VALIDATION_ANNOTATION.equals( annotationType ) ) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Provides a formatted string containing qualified names of enclosing types of provided methods.
	 *
	 * @param methods a collection of methods to convert to string of qualified names of enclosing types
	 * @return string of qualified names of enclosing types
	 */
	private String getEnclosingTypeElementQualifiedNames(Set<ExecutableElement> methods) {
		List<String> enclosingTypeElementQualifiedNames = CollectionHelper.newArrayList();
		for ( ExecutableElement method : methods ) {
			enclosingTypeElementQualifiedNames.add( getEnclosingTypeElementQualifiedName( method ) );
		}
		Collections.sort( enclosingTypeElementQualifiedNames );

		return StringHelper.join( enclosingTypeElementQualifiedNames, ", " );
	}

}
