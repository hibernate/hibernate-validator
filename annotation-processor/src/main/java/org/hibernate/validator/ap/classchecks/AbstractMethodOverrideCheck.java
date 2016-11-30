/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.classchecks;


import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.util.CollectionHelper;
import org.hibernate.validator.ap.util.ConstraintHelper;

/**
 * Abstract base class for {@link ClassCheck} implementations that check overridden methods.
 *
 * @author Marko Bekhta
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
	public Collection<ConstraintCheckIssue> checkMethod(ExecutableElement currentMethod) {
		if ( !needToPerformAnyChecks( currentMethod ) ) {
			return Collections.emptySet();
		}

		TypeElement currentTypeElement = getEnclosingTypeElement( currentMethod );

		// if current type is java.lang.Object then we can continue without doing any other checks
		if ( isJavaLangObjectOrNull( currentTypeElement ) ) {
			return Collections.emptySet();
		}
		// find if there's a method that was overridden by the current one.
		InheritanceTree overriddenMethodsTree = findAllOverriddenElements( currentTypeElement, currentMethod );
		if ( !overriddenMethodsTree.hasOverriddenMethods() ) {
			return Collections.emptySet();
		}

		return checkMethodInternal( currentMethod, overriddenMethodsTree );
	}

	/**
	 * Performs a real check of a method.
	 *
	 * @param currentMethod a method to check
	 * @param overriddenMethods a map of overridden methods received by calling {@link AbstractMethodOverrideCheck#findAllOverriddenElements(TypeElement, ExecutableElement)}
	 *
	 * @return a collection of issues if there are any, an empty collection otherwise
	 */
	protected abstract Collection<ConstraintCheckIssue> checkMethodInternal(ExecutableElement currentMethod, InheritanceTree overriddenMethods);

	/**
	 * There can be situations in which no checks should be performed. So in such cases we will not look for any overridden
	 * methods and do any work at all.
	 *
	 * @param currentMethod the method under investigation
	 *
	 * @return {@code true} if we should proceed with checks and {@code false} otherwise
	 */
	protected abstract boolean needToPerformAnyChecks(ExecutableElement currentMethod);

	/**
	 * Find overridden methods from all super classes and all implemented interfaces. Results are returned as {@link InheritanceTree}
	 *
	 * @param currentTypeElement the class in which the method is located
	 * @param currentMethod the method for which we want to find the overridden methods
	 *
	 * @return an {@link InheritanceTree} containing overridden methods
	 */
	private InheritanceTree findAllOverriddenElements(
			TypeElement currentTypeElement,
			ExecutableElement currentMethod) {
		InheritanceTree tree = new InheritanceTree( currentMethod, currentTypeElement );
		findAllOverriddenElementsRecursive( currentTypeElement, currentMethod, tree );
		return tree;
	}

	/**
	 * A recursive part of {@link AbstractMethodOverrideCheck#findAllOverriddenElements(TypeElement, ExecutableElement)}.
	 *
	 * @param currentTypeElement the class in which the method is located
	 * @param currentMethod the method for which we want to find the overridden methods
	 * @param tree a resulting inheritance tree
	 */
	private void findAllOverriddenElementsRecursive(
			TypeElement currentTypeElement,
			ExecutableElement currentMethod,
			InheritanceTree tree) {

		// look for implemented interfaces
		for ( Map.Entry<TypeElement, ExecutableElement> entry : findOverriddenMethodInInterfacesPairs(
				currentTypeElement,
				currentTypeElement.getInterfaces(),
				currentMethod
		).entrySet() ) {
			tree.addNode( entry.getValue(), entry.getKey() );
			findAllOverriddenElementsRecursive( entry.getKey(), entry.getValue(), tree );
		}

		TypeElement superType = (TypeElement) typeUtils.asElement( currentTypeElement.getSuperclass() );
		if ( isJavaLangObjectOrNull( superType ) ) {
			return;
		}

		ExecutableElement element = getOverriddenElement( currentTypeElement, superType, currentMethod );
		if ( element != null ) {
			tree.addNode( element, superType, currentTypeElement );
			findAllOverriddenElementsRecursive( superType, element, tree );
		}
	}

	/**
	 * Find pairs of enclosing type {@link TypeElement} and overridden method {@link ExecutableElement}  from implemented interfaces.
	 *
	 * @param currentTypeElement the class in which the method is located
	 * @param interfaces a list of implemented interfaces
	 * @param currentMethod the method for which we want to find the overridden methods
	 *
	 * @return a map of pairs of overridden methods (map key - an enclosing type, map value - overridden method in that type)
	 * if there are any, an empty map otherwise
	 */
	private Map<TypeElement, ExecutableElement> findOverriddenMethodInInterfacesPairs(
			TypeElement currentTypeElement,
			List<? extends TypeMirror> interfaces,
			ExecutableElement currentMethod) {
		Map<TypeElement,ExecutableElement> elements = CollectionHelper.newHashMap();

		for ( TypeMirror anInterface : interfaces ) {
			TypeElement implementedInterface = (TypeElement) typeUtils.asElement( anInterface );
			ExecutableElement element = getOverriddenElement( currentTypeElement, implementedInterface, currentMethod );
			if ( element != null ) {
				elements.put( implementedInterface, element );
			}
		}

		return elements;
	}

	/**
	 * Find a method that is overridden by the one passed to this function.
	 *
	 * @param currentTypeElement a class in which method is located
	 * @param otherTypeElement a class/interface on which to look for overridden method
	 * @param currentMethod the method for which we want to find the overridden methods
	 *
	 * @return an overridden method if there's one, and {@code null} otherwise
	 */
	private ExecutableElement getOverriddenElement(
			TypeElement currentTypeElement,
			TypeElement otherTypeElement,
			ExecutableElement currentMethod) {

		if ( isJavaLangObjectOrNull( otherTypeElement ) ) {
			return null;
		}

		for ( Element element : elementUtils.getAllMembers( otherTypeElement ) ) {
			if ( !element.getKind().equals( ElementKind.METHOD ) ) {
				continue;
			}
			if ( elementUtils.overrides( currentMethod, (ExecutableElement) element, currentTypeElement ) ) {
				return (ExecutableElement) element;
			}
		}

		return null;
	}

	/**
	 * Find a {@link TypeElement} that enclose a given {@link ExecutableElement}.
	 *
	 * @param currentMethod a method that you want to find class/interface it belongs to
	 *
	 * @return a class/interface represented by {@link TypeElement} to which a method belongs to
	 */
	private TypeElement getEnclosingTypeElement(ExecutableElement currentMethod) {
		return (TypeElement) typeUtils.asElement( currentMethod.getEnclosingElement().asType() );
	}

	/**
	 * Find a {@link String} representation of qualified name ({@link Name}) of corresponding {@link TypeElement} that enclose a given {@link ExecutableElement}.
	 *
	 * @param currentMethod a method that you want to find class/interface qualified name it belongs to
	 *
	 * @return a class/interface qualified name represented by {@link String} to which a method belongs to
	 */
	protected String getEnclosingTypeElementQualifiedName(ExecutableElement currentMethod) {
		return getEnclosingTypeElement( currentMethod ).getQualifiedName().toString();
	}

	/**
	 * Determine if provided type element ({@link TypeElement} represents a {@link java.lang.Object} or is {@code null}
	 *
	 * @param typeElement an element to check
	 *
	 * @return {@code true} if provided element is {@link java.lang.Object} or is {@code null}, {@code false} otherwise
	 */
	private boolean isJavaLangObjectOrNull(TypeElement typeElement) {
		return typeElement == null || JAVA_LANG_OBJECT.equals( typeElement.toString() );
	}

}
