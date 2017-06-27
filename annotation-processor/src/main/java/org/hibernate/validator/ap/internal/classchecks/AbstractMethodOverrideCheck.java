/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.classchecks;


import java.util.Collections;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.internal.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.internal.util.ConstraintHelper;

/**
 * Abstract base class for {@link ClassCheck} implementations that check overridden methods.
 *
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public abstract class AbstractMethodOverrideCheck extends AbstractClassCheck {

	private static final String JAVA_LANG_OBJECT = "java.lang.Object";

	private final Elements elementUtils;

	private final Types typeUtils;

	protected ConstraintHelper constraintHelper;

	public AbstractMethodOverrideCheck(Elements elementUtils, Types typeUtils, ConstraintHelper constraintHelper) {
		this.elementUtils = elementUtils;
		this.typeUtils = typeUtils;
		this.constraintHelper = constraintHelper;
	}

	@Override
	public Set<ConstraintCheckIssue> checkMethod(ExecutableElement currentMethod) {
		if ( !needToPerformAnyChecks( currentMethod ) ) {
			return Collections.emptySet();
		}

		// find if there's a method that was overridden by the current one.
		MethodInheritanceTree overriddenMethodsTree = findAllOverriddenElements( currentMethod );
		if ( !overriddenMethodsTree.hasOverriddenMethods() ) {
			return Collections.emptySet();
		}

		return checkMethodInternal( currentMethod, overriddenMethodsTree );
	}

	/**
	 * Performs the check of a method.
	 *
	 * @param currentMethod a method to check
	 * @param overriddenMethodsTree the {@link MethodInheritanceTree} of the method to check
	 *
	 * @return a set of issues if there are any, an empty set otherwise
	 */
	protected abstract Set<ConstraintCheckIssue> checkMethodInternal(ExecutableElement currentMethod, MethodInheritanceTree overriddenMethodsTree);

	/**
	 * There can be situations in which no checks should be performed. In such cases we will not perform any work at all.
	 *
	 * @param currentMethod the method under investigation
	 *
	 * @return {@code true} if we should proceed with checks and {@code false} otherwise
	 */
	protected abstract boolean needToPerformAnyChecks(ExecutableElement currentMethod);

	/**
	 * Find overridden methods from all super classes and all implemented interfaces. Results are returned as a {@link MethodInheritanceTree}.
	 *
	 * @param overridingMethod the method for which we want to find the overridden methods
	 *
	 * @return a {@link MethodInheritanceTree} containing overridden methods
	 */
	private MethodInheritanceTree findAllOverriddenElements(ExecutableElement overridingMethod) {
		TypeElement currentTypeElement = getEnclosingTypeElement( overridingMethod );
		MethodInheritanceTree.Builder methodInheritanceTreeBuilder = new MethodInheritanceTree.Builder( overridingMethod );

		collectOverriddenMethods( overridingMethod, currentTypeElement, methodInheritanceTreeBuilder );

		return methodInheritanceTreeBuilder.build();
	}

	/**
	 * Collect all the overridden elements of the inheritance tree.
	 *
	 * @param overridingMethod the method for which we want to find the overridden methods
	 * @param currentTypeElement the class we are analyzing
	 * @param methodInheritanceTreeBuilder the method inheritance tree builder
	 */
	private void collectOverriddenMethods( ExecutableElement overridingMethod, TypeElement currentTypeElement,
			MethodInheritanceTree.Builder methodInheritanceTreeBuilder) {
		if ( isJavaLangObjectOrNull( currentTypeElement ) ) {
			return;
		}

		collectOverriddenMethodsInInterfaces( overridingMethod, currentTypeElement, methodInheritanceTreeBuilder );

		TypeElement superclassTypeElement = (TypeElement) typeUtils.asElement( currentTypeElement.getSuperclass() );
		if ( superclassTypeElement == null ) {
			return;
		}

		ExecutableElement overriddenMethod = getOverriddenMethod( overridingMethod, superclassTypeElement );
		if ( overriddenMethod != null ) {
			methodInheritanceTreeBuilder.addOverriddenMethod( overridingMethod, overriddenMethod );
			overridingMethod = overriddenMethod;
		}

		collectOverriddenMethods( overridingMethod, superclassTypeElement, methodInheritanceTreeBuilder );
	}

	/**
	 * Collect overridden methods in the interfaces of a given type.
	 *
	 * @param overridingMethod the method for which we want to find the overridden methods
	 * @param currentTypeElement the class we are currently analyzing
	 * @param methodInheritanceTreeBuilder the method inheritance tree builder
	 */
	private void collectOverriddenMethodsInInterfaces(ExecutableElement overridingMethod, TypeElement currentTypeElement,
			MethodInheritanceTree.Builder methodInheritanceTreeBuilder) {
		for ( TypeMirror implementedInterface : currentTypeElement.getInterfaces() ) {
			TypeElement interfaceTypeElement = (TypeElement) typeUtils.asElement( implementedInterface );
			ExecutableElement overriddenMethod = getOverriddenMethod( overridingMethod, interfaceTypeElement );
			ExecutableElement newOverridingMethod;
			if ( overriddenMethod != null ) {
				methodInheritanceTreeBuilder.addOverriddenMethod( overridingMethod, overriddenMethod );
				newOverridingMethod = overriddenMethod;
			}
			else {
				newOverridingMethod = overridingMethod;
			}
			collectOverriddenMethodsInInterfaces( newOverridingMethod, interfaceTypeElement, methodInheritanceTreeBuilder );
		}
	}

	/**
	 * Find a method that is overridden by the one passed to this function.
	 *
	 * @param currentMethod the method for which we want to find the overridden methods
	 * @param typeElement the class or interface analyzed
	 * @return the overridden method if there is one, and {@code null} otherwise
	 */
	private ExecutableElement getOverriddenMethod(ExecutableElement currentMethod, TypeElement typeElement) {
		if ( typeElement == null ) {
			return null;
		}

		TypeElement enclosingTypeElement = getEnclosingTypeElement( currentMethod );

		for ( Element element : elementUtils.getAllMembers( typeElement ) ) {
			if ( !element.getKind().equals( ElementKind.METHOD ) ) {
				continue;
			}
			if ( elementUtils.overrides( currentMethod, (ExecutableElement) element, enclosingTypeElement ) ) {
				return (ExecutableElement) element;
			}
		}

		return null;
	}

	/**
	 * Find the {@link TypeElement} that contains a given {@link ExecutableElement}.
	 *
	 * @param currentMethod a method
	 * @return the class/interface containing the method represented by a {@link TypeElement}
	 */
	private TypeElement getEnclosingTypeElement(ExecutableElement currentMethod) {
		return (TypeElement) typeUtils.asElement( currentMethod.getEnclosingElement().asType() );
	}

	/**
	 * Find a {@link String} representation of qualified name ({@link Name}) of corresponding {@link TypeElement} that
	 * contains a given {@link ExecutableElement}.
	 *
	 * @param currentMethod a method
	 * @return a class/interface qualified name represented by {@link String} to which a method belongs to
	 */
	protected String getEnclosingTypeElementQualifiedName(ExecutableElement currentMethod) {
		return getEnclosingTypeElement( currentMethod ).getQualifiedName().toString();
	}

	/**
	 * Determine if the provided {@link TypeElement} represents a {@link java.lang.Object} or is {@code null}.
	 *
	 * @param typeElement the element to check
	 * @return {@code true} if the provided element is {@link java.lang.Object} or is {@code null}, {@code false} otherwise
	 */
	private boolean isJavaLangObjectOrNull(TypeElement typeElement) {
		return typeElement == null || JAVA_LANG_OBJECT.contentEquals( typeElement.getQualifiedName() );
	}

}
