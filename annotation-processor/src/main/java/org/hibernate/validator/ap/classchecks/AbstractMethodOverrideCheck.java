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
		Map<Boolean, List<ExecutableElement>> overriddenMethods = findAllOverriddenElements( currentTypeElement, currentMethod );
		if ( overriddenMethods.get( Boolean.TRUE ).isEmpty() && overriddenMethods.get( Boolean.FALSE ).isEmpty() ) {
			return Collections.emptySet();
		}

		return checkMethodInternal( currentMethod, overriddenMethods );
	}

	/**
	 * Performs a real check of a method.
	 *
	 * @param currentMethod a method to check
	 * @param overriddenMethods a map of overridden methods received by calling {@link AbstractMethodOverrideCheck#findAllOverriddenElements(TypeElement, ExecutableElement)}
	 *
	 * @return a collection of issues if there are any, an empty collection otherwise
	 */
	protected abstract Collection<ConstraintCheckIssue> checkMethodInternal(ExecutableElement currentMethod, Map<Boolean, List<ExecutableElement>> overriddenMethods);

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
	 * Find a list of overridden methods from all super classes and all implemented interfaces. Overridden methods are grouped into two lists
	 * with a predicate which determines whether a method is originally declared or is overriding some other method.
	 *
	 * @param currentTypeElement the class in which the method is located
	 * @param currentMethod the method for which we want to find the overridden methods
	 *
	 * @return a map that contains a pairs of {@link Boolean} and lists of overridden methods if there are any and a map with empty value lists otherwise.
	 * Both map keys will be present {@link Boolean#TRUE} and {@link Boolean#FALSE}. A list for {@link Boolean#TRUE} key will contain all overridden methods
	 * from implemented interfaces as well as from a class where the method was originally declared. A list for {@link Boolean#FALSE} will contain a all overridden
	 * methods that are overriding some other method on their own
	 */
	private Map<Boolean, List<ExecutableElement>> findAllOverriddenElements(
			TypeElement currentTypeElement,
			ExecutableElement currentMethod) {
		Map<Boolean, List<ExecutableElement>> elements = CollectionHelper.newHashMap();

		// get a super class. For interfaces this returns null
		TypeElement parentTypeElement = (TypeElement) typeUtils.asElement( currentTypeElement.getSuperclass() );

		// look for implemented interfaces:
		elements.put( Boolean.TRUE, findOverriddenMethodInInterfaces(
				currentTypeElement,
				currentTypeElement.getInterfaces(),
				currentMethod
		) );
		elements.put( Boolean.FALSE, CollectionHelper.newArrayList() );

		// this variable is used to search for possible overridden method from a supper class
		// and such that it is originally declared in that class and is not overriding any method
		// from some other super class or any implemented interface
		ExecutableElement possibleOriginalMethodFromClass = null;

		// if super class is java.lang.Object, then there's no need to do any other work: no such method was found.
		while ( !isJavaLangObjectOrNull( parentTypeElement ) ) {
			// need to check all the implemented interfaces as well
			List<ExecutableElement> overriddenInterfaceMethods = findOverriddenMethodInInterfaces( currentTypeElement, parentTypeElement.getInterfaces(), currentMethod );
			elements.get( Boolean.TRUE ).addAll( overriddenInterfaceMethods );

			ExecutableElement element = getOverriddenElement( currentTypeElement, parentTypeElement, currentMethod );
			if ( element != null ) {
				elements.get( Boolean.FALSE ).add( element );
				if ( overriddenInterfaceMethods.isEmpty() ) {
					possibleOriginalMethodFromClass = element;
				}
				else {
					possibleOriginalMethodFromClass = null;
				}
			}

			parentTypeElement = (TypeElement) typeUtils.asElement( parentTypeElement.getSuperclass() );
		}
		// if there's originally declared overridden method in some class we need to add it to corresponding list in the map
		if ( possibleOriginalMethodFromClass != null ) {
			elements.get( Boolean.TRUE ).add( possibleOriginalMethodFromClass );
		}

		return elements;
	}

	/**
	 * Find a list of overridden methods from implemented interfaces.
	 *
	 * @param currentTypeElement the class in which the method is located
	 * @param interfaces a list of implemented interfaces
	 * @param currentMethod the method for which we want to find the overridden methods
	 *
	 * @return a list of overridden methods if there are any, an empty list otherwise
	 */
	private List<ExecutableElement> findOverriddenMethodInInterfaces(
			TypeElement currentTypeElement,
			List<? extends TypeMirror> interfaces,
			ExecutableElement currentMethod) {
		List<ExecutableElement> elements = CollectionHelper.newArrayList();

		for ( TypeMirror anInterface : interfaces ) {
			ExecutableElement element = getOverriddenElement( currentTypeElement, (TypeElement) typeUtils.asElement( anInterface ), currentMethod );
			if ( element != null ) {
				elements.add( element );
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
